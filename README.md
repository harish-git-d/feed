# GAI Batch Project

A brand new Spring Batch scaffold for generating GAI 2.0 style files from feed definitions.

## What this project does

This starter project is designed around the flow you showed:

1. Load a feed definition.
2. Build three output files per feed:
   - EVENT
   - RECORD
   - ATTRIBUTE
3. Build a CONTROL file after the three data files are written.
4. Optionally transfer the files through SFTP.

## Project structure

- `config` - batch/job wiring and configuration properties
- `domain` - feed metadata and output model
- `service` - naming, file writing, transfer, and sample data generation
- `tasklet` - Spring Batch steps
- `src/main/resources/feed-definitions` - sample feed definitions in YAML

## Run locally

```bash
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--gai.feed-name=stress-exposure --gai.cob-date=2025-12-31 --gai.file-timestamp=20260122144002"
```

## Output files

By default the project writes to:

```text
./build/output
```

Example output naming:

```text
161534_CRC_SCEF-STRESSEXP_N_EVENT_DLY_F_SRC_20251231_20260122144002.dat.gz
161534_CRC_SCEF-STRESSEXP_N_RECORD_DLY_F_SRC_20251231_20260122144002.dat.gz
161534_CRC_SCEF-STRESSEXP_N_ATTRIBUTE_DLY_F_SRC_20251231_20260122144002.dat.gz
161534_CRC_SCEF-STRESSEXP_N_CONTROL_DLY_F_SRC_20251231_20260122144002.dat.gz
```

## Where to extend

- Replace `SampleDataFactory` with JDBC-based extraction from your Oracle/SCEF source.
- Replace sample feed YAML with one definition per real feed.
- Move SFTP credentials to a secret manager.
- Add separate EVENT / RECORD / ATTRIBUTE projection logic if each feed needs different fields.

## Sample feeds already wired

- `stress-exposure`
- `swwr`
- `pse-exposure`
- `swwr-recovery`
- `oet-flag`
- `pse-month-end`

## Overview
This Spring Batch application generates GAI (Global Aggregation Interface) feed files from database queries without requiring any database table creation permissions.

## Architecture Highlights

### No Database Table Creation
- **Read-only database access** - Application only queries existing tables
- **No Spring Batch metadata tables** - Uses in-memory job repository
- **No JPA entity tables** - All data models are DTOs for file generation
- **No persistence layer** - Application is stateless

### Database Requirements
- Single database connection with **SELECT** permission only
- Access to tables/views referenced in SQL queries under `src/main/resources/sql/`
- No need for DBA involvement or table creation

### Job Execution Tracking
Since Spring Batch metadata tables are not used:
- Job execution history is **not persisted** to database
- Each run is independent with no state carried between executions
- Job status is logged to application logs only
- Use external monitoring/orchestration tools for job tracking

## Configuration

### Minimum Required Environment Variables
