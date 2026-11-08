#!/bin/bash
# ====================================================================
# Retail Microservices - Run End-to-End Tests
# ====================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "===================================================================="
echo "Starting Retail Microservices End-to-End Test Suite"
echo "===================================================================="

node "$SCRIPT_DIR/e2e-test-runner.js" "$@"

