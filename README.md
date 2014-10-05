# CheckAkkaBehavior
Small application to check the behaviors of <a href="http://akka.io/">akka</a> actor scheduler and dispacher.

## Usage 

### Run
To run without sbt, at first execute "sbt stage". Then run target/start(.bat).

### Settings
```
Optional arguments:
    -a #    : number of actors
    -n #    : number of messages (per actor)
    -w #    : weight of computation

Optional arguments (Akka default-dispatcher):
    -at #   : <akka> throughput
    -apf #  : <akka> parallelism-factor
    -apmin #: <akka> parallelism-min
    -apmax #: <akka> parallelism-max
```

### Example
Report format:
 [actor_no: #message_no at thread_no]

```
% target/start -a 3 -n 4 -at 2 -apmax 2
=== [Akka Configuration] ===
throughput: 2
factor: 3.0 [Min 8.0 -> 2.0 Max]
=== [System Information] ===
Processors: 8
Thread active: 4
Current Thread: 1

=== [Manager report] ===
[ 0: #000 at   10]      13 ->     599
[ 1: #000 at   11]      13 ->     600
[ 1: #001 at   10]     600 ->    1132
[ 2: #000 at   11]     600 ->    1137
[ 1: #002 at   10]    1132 ->    1683
[ 2: #001 at   11]    1140 ->    1692
[ 0: #001 at   10]    1684 ->    2254
[ 2: #002 at   11]    1692 ->    2260
[ 0: #002 at   10]    2254 ->    2791
[ 2: #003 at   11]    2260 ->    2794
[ 0: #003 at   11]    2796 ->    3321
[ 1: #003 at   10]    2791 ->    3322

=== [Thread report] ===
 # Thread 10
[ 0: #000 at   10]      13 ->     599
[ 1: #001 at   10]     600 ->    1132
[ 1: #002 at   10]    1132 ->    1683
[ 0: #001 at   10]    1684 ->    2254
[ 0: #002 at   10]    2254 ->    2791
[ 1: #003 at   10]    2791 ->    3322
 # Thread 11
[ 1: #000 at   11]      13 ->     600
[ 2: #000 at   11]     600 ->    1137
[ 2: #001 at   11]    1140 ->    1692
[ 2: #002 at   11]    1692 ->    2260
[ 2: #003 at   11]    2260 ->    2794
[ 0: #003 at   11]    2796 ->    3321

=== [Actor report] ===
 # Actor 0
[ 0: #000 at   10]      13 ->     599
[ 0: #001 at   10]    1684 ->    2254
[ 0: #002 at   10]    2254 ->    2791
[ 0: #003 at   11]    2796 ->    3321
 # Actor 1
[ 1: #000 at   11]      13 ->     600
[ 1: #001 at   10]     600 ->    1132
[ 1: #002 at   10]    1132 ->    1683
[ 1: #003 at   10]    2791 ->    3322
 # Actor 2
[ 2: #000 at   11]     600 ->    1137
[ 2: #001 at   11]    1140 ->    1692
[ 2: #002 at   11]    1692 ->    2260
[ 2: #003 at   11]    2260 ->    2794
Finished all tasks.
```

## License
Licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0.html">Apache 2.0 License</a>.
