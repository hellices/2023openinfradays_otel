# 2023openinfradays_otel
- 본 페이지는 2023년도 OpenInfra Community Days Korea 2023에서 시연한 내용을 직접 구성해볼 수 있도록 준비한 자료입니다.  
- 구성에 대하여 문의는 이슈를 통해 주시면 최대한 답변드리겠습니다.

## Opentelemetry와  Grafana, prometheus 등을 활용한 대시보드 구성
<img src="./static/otel.drawio.svg" alt="" >   

- server, client 두 개의 서비스를 기동하여 서비스 간 통신을 그라파나 대시보드를 통해 모니터링/디버깅하는 예제를 구성해 볼 수 있습니다.
- 두 서버는 각각 [opentelemetry instrument](https://opentelemetry.io/docs/zero-code/java/) sdk를 javaagent에 설정하여 기동합니다.
- opentelemetry sdk(.jar)는 다음과 같은 역할을 수행합니다.
1. metric을 노출시켜 prometheus에서 scraping 가능하도록 합니다.
2. log 내에 trace_id를 보내줍니다.(java mdc - mapped diagnostic context 활용)
3. trace정보를 수집 가능한 서버로 전송합니다. 

### OpenTelemetry 4대 신호 (Four Signals)
> OpenTelemetry는 기존 Metrics, Logs, Traces 3가지 신호에서 **Profiling**을 추가하여 총 4가지 신호를 지원합니다(experimental).   
> - **Metrics** : 시스템의 상태를 수치로 표현 (prometheus)   
> - **Logs** : 애플리케이션 이벤트 기록 (loki)   
> - **Traces** : 분산 시스템에서 요청의 흐름 추적 (tempo)   
> - **Profiling** : CPU, 메모리 등 코드 레벨의 성능 분석 ([Grafana Pyroscope](https://grafana.com/docs/pyroscope/latest/))   
>
> eBPF(extended Berkeley Packet Filter)를 활용하면 애플리케이션 코드 변경 없이 커널 수준에서 전체 시스템을 프로파일링할 수 있습니다.   
> - [OTel eBPF Instrumentation](https://github.com/open-telemetry/opentelemetry-ebpf-instrumentation): 코드 변경 없이 k8s 노드 전체를 eBPF로 계측   

### 00. 프로젝트 구성
| path       |  용도    |
|------------|---------|
| /client    | client application 빌드용 |
| /ebpf      | OTel eBPF instrumentation helm chart value |
| /grafana   | 대시보드 템플릿 |
| /loki      | loki helm chart value |
| /otel      | opentelemetry autoinstrumentation을 위한 custom resource definition |
| /prometheus| prometheus helm chart value |
| /pyroscope | pyroscope(continuous profiling) helm chart value |
| /server    | server application 빌드용 |
| /tempo     | tempo helm chart value |

### 01. pre-req. 각주 참고하여 설치
- kubernetes[^kubernetes]   * [참고](/static/img_01.png)
- helm[^helm]
- java 17[^java]
- vs code[^vscode] or gradle[^gradle]

[^kubernetes]: [docker desktop 설치](https://docs.docker.com/desktop/install),  kubernetes 활성화 : 설정 -> kubernetes -> Enable Kubernetes    
[^helm]: [helm install](https://helm.sh/docs/intro/install/)   
[^java]: [temurin java](https://adoptium.net/temurin/releases/)   
[^vscode]: [vs code](https://code.visualstudio.com/download)   
[^gradle]: [gradle](https://gradle.org/install/)   

### 02. monitoring tool install
진행 시 로컬PC의 docker desktop과 kubernetes를 사용하므로 기동중이어야 함.

- [prometheus](https://github.com/prometheus-community/helm-charts)
- [grafana](https://github.com/grafana/helm-charts)
- [loki single replica](https://grafana.com/docs/loki/latest/setup/install/helm/install-monolithic/)
- [alloy](https://grafana.com/docs/alloy/latest/set-up/install/kubernetes/)
- [tempo](https://grafana.com/docs/tempo/latest/set-up-for-tracing/setup-tempo/deploy/kubernetes/helm-chart/)
- [pyroscope](https://grafana.com/docs/pyroscope/latest/deploy-kubernetes/helm/) *(continuous profiling - 4th OTel signal)*
- [opentelemetry-ebpf-instrumentation](https://github.com/open-telemetry/opentelemetry-ebpf-instrumentation) *(zero-code eBPF 계측)*

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts

kubectl create namespace metric
helm upgrade -i prometheus prometheus-community/prometheus -f ./prometheus/values.yaml -n metric
helm upgrade -i  grafana grafana/grafana -n metric
helm upgrade -i  -f loki/values.yaml loki grafana/loki -n metric
helm upgrade -i  alloy grafana/alloy -n metric
helm upgrade -i  -f tempo/values.yaml tempo grafana/tempo -n metric
helm upgrade -i  -f pyroscope/values.yaml pyroscope grafana/pyroscope -n metric
helm upgrade -i  -f ebpf/values.yaml opentelemetry-ebpf-instrumentation open-telemetry/opentelemetry-ebpf-instrumentation -n metric

```
> prometheus : cpu, memory 데이터 수집 + server_http_duration_bucket 과 exemplars 로 지연 api 확인 **metric 저장소**   
> alloy + loki : alloy가 로그를 수집(promtail의 후속 제품), loki에 저장(elasticsearch 등 대체 가능) **log 저장소**   
> tempo : 분산추적을 위한 저장소. (zipkin&jaeger 등으로 대체 가능) **trace 저장소**   
> pyroscope : 연속 프로파일링(CPU/메모리/코드 핫스팟) 저장소. **profile 저장소**   
> grafana : 위 네 종류의 telemetry를 조합하여 시각화.   
> opentelemetry-ebpf-instrumentation : 코드 변경 없이 eBPF로 전체 노드의 네트워크/성능 데이터를 수집하여 tempo/prometheus로 전송.

### 03. application deploy

- server, client application은 아래와 같이 커맨드라인으로 빌드하거나 vs code에서 gradle extension을 설치해서 빌드한다.
- database(h2)는 server와 같이 기동되며 인메모리로 실행
- vs code -> extension -> [gradle for java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-gradle) 설치
- [jib 빌드](https://github.com/GoogleContainerTools/jib)는 링크를 참고

- server 는 [otel Autoinstrumentation](https://opentelemetry.io/docs/platforms/kubernetes/operator/automatic/) 방식으로 배포하였으며 다음과 같은 작업 필요.   
> helm으로 otel operator 설치
> Autoinstrumentation을 위한 crd(custom resource definition)설치
> server 배포

- client의 경우 opentelemetry.jar를 docker image에 추가하는 방식으로 진행.

> autoinstrumentation과 docker build 중 택일 가능

```bash 
# client application 배포
cd client
sudo ./gradlew jibDockerBuild
kubectl apply -f ./kube.yaml

# server app 배포 전 opentelemetry autoinstrumentation을 위한 환경 셋팅
cd ..
kubectl create namespace otel
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts
helm upgrade -i opentelemetry-operator open-telemetry/opentelemetry-operator -n otel --set admissionWebhooks.certManager.enabled=false --set admissionWebhooks.autoGenerateCert.enabled=true --wait
kubectl apply -f otel/crd.yaml

# server application 배포
cd server
sudo ./gradlew jibDockerBuild
kubectl apply -f ./kube.yaml
```

### 04. application test

```bash
kubectl port-forward svc/spring-client 8080:8080 -n metric
```

another shell
```bash
curl localhost:8080/call # 전체 조회
curl "localhost:8080/call/one?name=microsoft" # 단건 조회. 응답 5초 지연
curl "localhost:8080/call/one?name=google" # 단건 조회. 응답 5초 지연
curl "localhost:8080/call/abc" # 404 error
curl "localhost:8080/call/404" # client -> server 호출 시 오류 페이지 조회. client에서는 not found 리턴
```

### 05. grafana setting

```bash
# grafana admin password 확인
kubectl get secret --namespace metric grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo 
# grafana 접속
kubectl port-forward svc/grafana 7777:80 -n metric
```

> http://localhost:7777 에 접속한다.   
> ID : admin / password : 위에서 확인   
> connections > data sources > Loki, Prometheus, Tempo를 각각 설정   

### 06. datasource configuration

- loki -> url(http://loki:3100)   
- Derived fields에 아래와 같이 입력   
<img src="./static/loki.png">   

- tempo -> url(http://tempo:3100)   
- Service graph -> datasource에 prometheus 추가.   
- 이 때 prometheus는 remote write(-- web.enable-remote-write-receiver)가 활성화되어 있어야 하고 tempo에서 service graph 기능(metricsGenerator)이 켜져야 한다.(각각 value.yaml 참고)   
<img src="./static/tempo.png">    

- prometheus -> url(http://prometheus-server)   
- Exemplars -> tempo 추가   
<img src="./static/prometheus.png">   

- pyroscope -> url(http://pyroscope:4040)   
- Grafana에서 Pyroscope 데이터소스 추가 후 flamegraph 대시보드를 통해 CPU/메모리 핫스팟 확인 가능   
- [Pyroscope 데이터소스 문서](https://grafana.com/docs/grafana/latest/datasources/pyroscope/)

### 07. dashboard import
- grafana -> dashboards -> new -> import
- ./grafana/dashboard.json add


### 08. 삭제

```bash

helm delete tempo -n metric
helm delete alloy -n metric
helm delete prometheus -n metric
helm delete opentelemetry-operator -n otel
helm delete opentelemetry-ebpf-instrumentation -n metric
helm delete loki -n metric
helm delete pyroscope -n metric
helm delete grafana -n metric
kubectl delete -f otel/crd.yaml
kubectl delete -f server/kube.yaml
kubectl delete -f client/kube.yaml
kubectl delete namespace otel
kubectl delete namespace metric

```