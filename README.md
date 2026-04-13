# kuksa-sensor

Java gRPC client that reads sensor values from a file, publishes them as datapoints to a KUKSA Databroker, and writes the publish results to `all_signals.csv`.

## Prerequisites

- Java 21+
- Maven 3.9+
- A running KUKSA Databroker instance with a VSS catalog loaded

## Start the Databroker

If you have the databroker binary built locally:

```bash
/path/to/kuksa-databroker/target/release/databroker --vss /path/to/kuksa-databroker/vss.json
```

Expected log line:

```text
Listening on 0.0.0.0:55555
```

## Configuration

The app reads `sensor_config.json` by default (or a path passed as the first argument).

```json
{
  "server": "127.0.0.1:55555",
  "timeout_ms": 10000,
  "thread_pool_size": 4,
  "signals": [
    {
      "path": "Vehicle.Speed",
      "interval_ms": 100,
      "values_file": "values.txt"
    }
  ]
}
```

| Field             | Required | Default | Description                                      |
|-------------------|----------|---------|--------------------------------------------------|
| `server`          | yes      | —       | Databroker address (`host:port`)                 |
| `timeout_ms`      | no       | 10000   | Per-call gRPC deadline in milliseconds           |
| `thread_pool_size`| no       | 8       | Scheduler thread pool size                       |
| `signals`         | yes      | —       | List of signals to publish                       |

Each signal entry:

| Field          | Required | Description                                              |
|----------------|----------|----------------------------------------------------------|
| `path`         | yes      | VSS signal path (e.g. `Vehicle.Speed`)                   |
| `interval_ms`  | yes      | Publish interval in milliseconds (must be > 0)           |
| `values_file`  | yes      | Path to a text file with one float value per line        |

### values_file format

One numeric value per line. The app publishes them in order, one per interval tick, and stops when the last value has been published.

```
10.5
20.0
35.7
```

## Run

```bash
mvn compile exec:java -Dexec.mainClass="com.kuksa.app.SensorApp" -Dexec.args="sensor_config.json" -Dexec.jvmArgs="--enable-native-access=ALL-UNNAMED"
```

The app will:
1. Load and validate the config
2. Read each signal's values from its `values_file`
3. Schedule each signal on its own fixed-rate timer
4. Publish every value to the databroker via gRPC
5. Wait until all signals have published their last value
6. Write results to `all_signals.csv` and exit

## Output

Results are written to `all_signals.csv` in the project root:

```
signal,seq,t_pub_us,value
Vehicle.Speed,1,1713000000000000,10.5
Vehicle.Speed,2,1713000000100000,20.0
...
```

| Column      | Description                              |
|-------------|------------------------------------------|
| `signal`    | VSS signal path                          |
| `seq`       | Sequence number (1-based)                |
| `t_pub_us`  | Publish timestamp in microseconds (epoch)|
| `value`     | Published float value                    |

## Notes

- The databroker must be started with a VSS catalog that includes all configured signal paths, otherwise publishes will fail with `NOT_FOUND`.
- If `timeout_ms` or `thread_pool_size` are omitted from the config, defaults of `10000` ms and `8` threads are applied automatically.
- If the databroker runs in a container, ensure port `55555` is forwarded to the host.
