# Retail Microservices System — Performance & NFR Load Testing

This test suite validates compliance with **Non-Functional Requirements (NFR 5.1 & 5.2)**:
* **Throughput**: Each microservice handles $\ge 100$ transactions / second.
* **Response Time**: $p95 \le 200\text{ ms}$ for 95% of operations ($< 2\text{ seconds}$ for distributed checkout Saga).
* **Availability**: $\ge 99.9\%$ operational uptime under sustained concurrency.

---

## 1. Quickstart with Locust (Python)

### 1.1. Prerequisites
Ensure Python 3.9+ is installed:
```bash
pip install locust
```

### 1.2. Headless Automated Benchmark (100 Concurrent Users)
```bash
locust -f performance-tests/locustfile.py \
  --host http://localhost:8080 \
  --users 100 \
  --spawn-rate 10 \
  --run-time 2m \
  --headless \
  --html performance-tests/report.html
```

### 1.3. Interactive Web UI Mode
```bash
locust -f performance-tests/locustfile.py --host http://localhost:8080
```
Open [http://localhost:8089](http://localhost:8089) in your browser to inspect real-time response time graphs, requests-per-second, and p95 latency charts.

