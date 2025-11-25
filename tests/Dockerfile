FROM alpine:3.18

RUN apk add --no-cache curl jq bash python3

WORKDIR /scripts
COPY scripts /scripts
RUN chmod +x /scripts/check_endpoints.sh

ENTRYPOINT ["/bin/bash", "/scripts/check_endpoints.sh"]
