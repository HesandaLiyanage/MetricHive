name: Bug Report
description: Create a report to help us improve MetricHive
title: "[BUG] "
labels: ["bug"]
body:
  - type: markdown
    attributes:
      value: Thanks for taking the time to fill out this bug report!
  - type: textarea
    id: description
    attributes:
      label: Bug Description
      description: A clear and concise description of what the bug is.
    validations:
      required: true
  - type: textarea
    id: reproduction
    attributes:
      label: Steps to Reproduce
      description: Steps to reproduce the behavior.
      placeholder: |
        1. Start cluster with '...'
        2. Send POST request with payload '...'
        3. Observe error code '...'
    validations:
      required: true
  - type: textarea
    id: expected
    attributes:
      label: Expected Behavior
      description: A clear and concise description of what you expected to happen.
    validations:
      required: true
  - type: textarea
    id: logs
    attributes:
      label: Relevant Logs & Error Output
      description: Please copy and paste any relevant logs from kubectl or docker compose.
      render: shell
