# GAI Batch Feed Project

Spring Batch 5 application that generates GAI 2.0 ADIL feed files from the SCEF Oracle database.

## Architecture

One job (`gaiFeedJob`) runs per feed, per day:

```
loadDefinitionStep    → loads feed YAML from classpath
generateEventStep     → queries DB, writes EVENT .dat.gz
generateRecordStep    → queries DB, writes RECORD .dat.gz
generateAttributeStep → queries DB, writes ATTRIBUTE .dat.gz
generateControlStep   → writes CONTROL .dat.gz  (GAI processing trigger)
transferFilesStep     → SFTP: data files first, control file last
```

## File naming

```
161534_CRC_SCEF-{MODULE}_N_{CATEGORY}_{FREQ}_F_SRC_{COBDATE}_{TIMESTAMP}.dat.gz
```

Example:
```
161534_CRC_SCEF-STRESSEXP_N_EVENT_DLY_F_SRC_20251231_20260122144002.dat.gz
161534_CRC_SCEF-STRESSEXP_N_RECORD_DLY_F_SRC_20251231_20260122144002.dat.gz
161534_CRC_SCEF-STRESSEXP_N_ATTRIBUTE_DLY_F_SRC_20251231_20260122144002.dat.gz
161534_CRC_SCEF-STRESSEXP_N_CONTROL_DLY_F_SRC_20251231_20260122144002.dat.gz
```

## Feeds

| Feed name       | Module  | Frequency |
|----------------|---------|-----------|
| stress-exposure | STRESSEXP | Daily   |
| swwr-flag       | SWWR      | Daily   |
| pse-exposure    | PSE       | Daily   |
| swwr-recovery   | SWWRRCY   | Daily   |
| oet-flag        | OET       | Daily   |
| pse-month-end   | PSEME     | Monthly |

## Running

```bash
# DEV - single feed
java -jar gai-batch-project.jar \
  --spring.profiles.active=dev \
  --gai.feed-name=stress-exposure \
  --gai.cob-date=20251231 \
  --gai.file-timestamp=20260122144002

# Or via env vars
GAI_FEED_NAME=stress-exposure GAI_COB_DATE=20251231 GAI_FILE_TIMESTAMP=20260122144002 \
  java -jar gai-batch-project.jar --spring.profiles.active=dev
```

To run all 5 daily feeds wrap in a shell loop or scheduler that calls the jar once per feed name.

## SQL files

Each feed × category needs a SQL file at:
```
src/main/resources/sql/{feedName}_{category}_query.sql
```

The SQL must accept a single positional `?` parameter for the COB date (YYYYMMDD string).

Files already present (stub queries, replace with real SCEF table names):
- `stress-exposure_{event,record,attribute}_query.sql`
- `swwr-flag_{event,record,attribute}_query.sql`
- `pse-exposure_{event,record,attribute}_query.sql`
- `swwr-recovery_{event,record,attribute}_query.sql`
- `oet-flag_{event,record,attribute}_query.sql`
- `pse-month-end_{event,record,attribute}_query.sql`

## SFTP

Set `gai.sftp.enabled=true` and configure host/credentials per environment profile.
The control file is always sent last — GAI uses it as the processing trigger.

## Pending from GAI team

- `gai.sftp.remote-directory` — SFTP target path not yet confirmed
- `pse-month-end` frequency calendar — confirm last BD vs last calendar day rule
