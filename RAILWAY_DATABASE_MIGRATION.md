# Migration Database on Railway

## Situation

You have modified the database schema for `assistance_values` table, changing from a position-based index to a `participant_id` based key. Since Railway PostgreSQL persists data, the old schema still exists.

## Solution

Flyway migrations have been added to your project. They will run automatically on the next deployment:

### What will happen:

1. **V1__Initial_schema.sql** - Creates the initial schema with the new structure
2. **V2__Fix_assistance_values_schema.sql** - Drops the old tables and recreates them with the new structure

### Manual steps if needed:

1. **Option A: Automatic (Recommended)**
   - Just deploy the code to Railway
   - The migrations will run automatically on startup

2. **Option B: Manual cleanup**
   - Connect to your Railway PostgreSQL database
   - Run these commands:

   ```sql
   -- Drop old tables
   DROP TABLE IF EXISTS assistance_values CASCADE;
   DROP TABLE IF EXISTS meeting_assistance CASCADE;

   -- The new tables will be created by Flyway on next deployment
   ```

### Configuration:

- `application.properties` now includes:
  - `spring.jpa.hibernate.ddl-auto=validate` (instead of `update`)
  - Flyway is enabled and configured to use migrations from `classpath:db/migration`

### Next deployment:

When you push to Railway or redeploy:
1. Flyway will check which migrations have been applied
2. It will run any new migrations in order
3. The new schema will be in place

## For future changes:

For any future database schema changes:
1. Create a new migration file: `V3__Your_change_description.sql`
2. Commit and push to Railway
3. The migration will run automatically

---

**Important:** Make sure to backup your data before deploying if you have production data!
