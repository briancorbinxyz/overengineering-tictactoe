#!/usr/bin/env sh
# Wrapper to run the benchmark workload.
# Delegates to app/scripts/bench.sh and forwards all arguments.

"$(dirname "$0")"/../app/scripts/bench.sh "$@"
