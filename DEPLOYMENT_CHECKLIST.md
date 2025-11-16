# Deployment Checklist - Railway Backend + GitHub Pages Frontend

## Current Status
- ✅ Backend: Fixed database credential parsing, JAR compiled and packaged
- ✅ Frontend: npm build successful, .env.production updated
- ✅ CORS: Already configured in SecurityConfig.java for `https://fhenouille.github.io`

## Prerequisites
Before starting deployment, you need:
1. Railway account with backend service created (or create a new project)
2. GitHub repository for frontend with GitHub Pages enabled
3. Your GitHub Pages URL (example: `https://fhenouille.github.io/zoom`)

## Step 1: Prepare for Backend Deployment

### 1.1 Create/Update Railway Backend Service
- Go to your Railway dashboard
- Create a new service for the backend (if not already created)
- Obtain your Railway backend domain (example: `https://zoom-backend-xyz123.up.railway.app`)

### 1.2 Add PostgreSQL Add-on to Railway
- In Railway dashboard, add PostgreSQL add-on to your backend service
- This automatically creates a `DATABASE_URL` environment variable
- The DatabaseUrlEnvironmentPostProcessor will automatically parse this

### 1.3 Update Frontend Environment
Once you have your actual Railway backend URL:
```bash
# In frontend/.env.production
VITE_API_BASE_URL=https://zoom-backend-xyz123.up.railway.app/api
```

Replace `zoom-backend-xyz123` with your actual Railway service identifier.

### 1.4 Verify Application Properties
Check that `application.properties` has correct defaults:
```properties
# These will be overridden by EnvironmentPostProcessor if DATABASE_URL is set
spring.datasource.url=jdbc:postgresql://localhost:5432/zoom
spring.datasource.username=postgres
spring.datasource.password=postgres

# These MUST match what you set in application.properties
zoom.api.user-id=${ZOOM_USER_ID:default-user-id}
zoom.api.account-id=${ZOOM_ACCOUNT_ID:default-account-id}
```

## Step 2: Commit All Changes

### 2.1 Stage all changes
```bash
cd c:\Users\fhenouille\work\git\zoom
git add .
```

### 2.2 Commit with descriptive message
```bash
git commit -m "Fix: Database credential extraction for Railway PostgreSQL + Configure frontend for GitHub Pages deployment"
```

### 2.3 Push to GitHub
```bash
git push origin main
```

## Step 3: Deploy Backend to Railway

### 3.1 Push Backend Code
```bash
# Option A: Connect Railway to GitHub repository
# In Railway dashboard: Select your service → GitHub → Connect repository
# Railway will auto-build when you push

# Option B: Deploy JAR directly
# Upload target/zoom-backend-0.0.1-SNAPSHOT.jar to Railway
```

### 3.2 Verify Environment Variables
In Railway dashboard:
- Confirm `DATABASE_URL` is automatically set by PostgreSQL add-on
- It should look like: `postgresql://user:password@postgres.railway.internal:5432/railway`
- The DatabaseUrlEnvironmentPostProcessor will automatically extract credentials

### 3.3 Monitor Startup
- Check Railway logs for startup messages
- Look for: "DatabaseUrlEnvironmentPostProcessor" initialization logs
- Verify: Tables created successfully by Hibernate
- Verify: Default admin user created by DataInitializer

Expected log messages:
```
Creating datasource with URL: jdbc:postgresql://postgres.railway.internal:5432/railway
Setting datasource username: <extracted-username>
Setting datasource password: <masked>
DataInitializer: Creating default admin user...
```

## Step 4: Deploy Frontend to GitHub Pages

### 4.1 Ensure .env.production is Correct
Verify your `.env.production` has the Railway backend URL:
```env
VITE_API_BASE_URL=https://zoom-backend-xyz123.up.railway.app/api
VITE_APP_NAME=Zoom Meetings
```

### 4.2 Push Frontend Code
```bash
git push origin main
```

This automatically triggers GitHub Pages deployment.

### 4.3 Verify GitHub Pages is Enabled
- Go to GitHub repository → Settings → Pages
- Confirm: Source is set to "GitHub Actions" or main branch
- Confirm: Custom domain is set (if applicable)
- Confirm: Enforce HTTPS is enabled

### 4.4 Access Frontend
Your frontend will be available at:
- `https://fhenouille.github.io/zoom` (if in a repo called `zoom`)
- or check your repository settings for exact URL

## Step 5: Verify Cross-Domain Communication

### 5.1 Test API Connection
1. Navigate to your GitHub Pages URL in a browser
2. Open Developer Tools (F12) → Console
3. Verify no CORS errors in console
4. Test login or meeting features

Expected flow:
- GitHub Pages frontend loads successfully
- Frontend makes API request to `https://zoom-backend-xyz123.up.railway.app/api/...`
- CORS headers allow request (SecurityConfig.java allows GitHub Pages origin)
- Backend responds with data

### 5.2 Common Issues and Solutions

**Issue: CORS Error**
- Ensure SecurityConfig.java allows `https://fhenouille.github.io`
- Current config: ✅ Already includes this
- If using custom domain: Add to SecurityConfig.java allowedOrigins

**Issue: 404 Not Found**
- Verify the Railway backend URL is correct
- Verify backend service is running (check Railway dashboard)
- Verify database connection is successful (check logs)

**Issue: Database Connection Error**
- Check Railway logs for DATABASE_URL parsing
- Verify PostgreSQL add-on is active
- Verify DatabaseUrlEnvironmentPostProcessor is running
- Look for: "Creating datasource with URL" in logs

**Issue: 502 Bad Gateway**
- Backend service may be crashing
- Check Railway logs for startup errors
- Common causes:
  - Missing DATABASE_URL environment variable
  - Invalid Zoom API credentials
  - Port 8080 is not available

## Step 6: Optional - Update CORS for Custom Domain

If you're using a custom domain for frontend:

```java
// In SecurityConfig.java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173",
    "http://localhost:3000",
    "https://fhenouille.github.io",
    "https://your-custom-domain.com"  // Add your custom domain
));
```

## Step 7: Production Configuration

### 7.1 Set Zoom API Credentials on Railway
In Railway dashboard, set environment variables:
```
ZOOM_CLIENT_ID=your-actual-client-id
ZOOM_CLIENT_SECRET=your-actual-client-secret
ZOOM_ACCOUNT_ID=your-actual-account-id
ZOOM_USER_ID=your-actual-user-id
```

These are required for Zoom API integration to work.

### 7.2 Disable Debug Logging (Optional)
Update `application.properties` for production:
```properties
logging.level.root=WARN
logging.level.com.zoom=INFO
logging.level.org.hibernate.SQL=WARN
```

## Deployment Summary

```
GitHub Pages Frontend                    Railway Backend
https://fhenouille.github.io/zoom   →    https://zoom-backend-xyz.up.railway.app
        ↓
   React + Vite                          Spring Boot 3.2
   VITE_API_BASE_URL set to:            PostgreSQL (via Railway add-on)
   Railway backend domain                Automatic credential parsing
        ↓
   .env.production                       EnvironmentPostProcessor
   Built into JavaScript                 SecurityConfig CORS enabled
        ↓
   Browser makes request                 Backend processes request
   https://zoom-backend-xyz...           and returns response
```

## Files Modified for This Deployment

1. **backend/src/main/java/com/zoom/config/DatabaseUrlEnvironmentPostProcessor.java**
   - ✅ Fixed: lastIndexOf(':') for correct credential parsing
   - ✅ Fixed: Removed credentials from JDBC URL

2. **backend/src/main/java/com/zoom/entity/DataInitializer.java**
   - ✅ Fixed: Property names match application.properties (zoom.api.*)

3. **backend/src/main/java/com/zoom/config/SecurityConfig.java**
   - ✅ Already configured: CORS allows GitHub Pages origin

4. **frontend/.env.production**
   - ✅ Updated: VITE_API_BASE_URL set to Railway backend domain

## Next Steps After Deployment

1. **Monitor Logs**: Watch Railway dashboard logs for 24 hours
2. **Test Features**: Test login, create meetings, verify database operations
3. **Load Testing**: Test with multiple concurrent users (if applicable)
4. **Performance Monitoring**: Check response times from GitHub Pages
5. **Error Tracking**: Set up error logging/monitoring (optional)

## Rollback Plan

If deployment has issues:

1. **Backend**: Revert to previous Railway deployment in dashboard
2. **Frontend**: GitHub Pages automatically rolls back to previous build
3. **Code**: Use `git revert` to undo changes and push again

## Support

If you encounter issues during deployment:
1. Check Railway dashboard logs for backend errors
2. Check GitHub Actions logs for frontend build failures
3. Check browser console (F12) for frontend errors
4. Review the fix documentation in conversation history

