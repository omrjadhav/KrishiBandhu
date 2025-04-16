# Plant Shop App with Supabase Integration

This Android application uses Supabase as a backend for authentication, database, and storage.

## Setup Instructions

### 1. Supabase Project Setup

1. Create a Supabase project at https://supabase.com
2. Go to "Project Settings" > "API" to obtain your:
   - Supabase URL (`https://your-project-id.supabase.co`)
   - Supabase anon key (starts with `eyJ...`)
3. Go to the SQL Editor and run the SQL commands from the `supabase_setup.sql` file to create the required tables and security policies

### 2. Storage Bucket Setup

1. Go to "Storage" in your Supabase dashboard
2. Create a new bucket called `plant-images`
3. Set the bucket's privacy to "Public" 

### 3. Update Project Configuration

1. In `app/src/main/java/com/example/basicmod/data/api/SupabaseClient.kt`, update the `SUPABASE_URL` and `SUPABASE_KEY` constants with your Supabase project values:

```kotlin
private const val SUPABASE_URL = "https://your-project-id.supabase.co"
private const val SUPABASE_KEY = "your-anon-key"
```

### 4. Build and Run

1. Build and run the application
2. The app will create a test owner account (`owner@test.com` with password `password123`) and populate sample plant data

## API Integration Details

The app uses the following Supabase features:

### Authentication (GoTrue)
- User signup and login with email/password
- Session management

### Database (PostgreSQL)
- User profiles storage in `user_profiles` table
- Plant data storage in `plants` table

### Storage
- Plant images stored in the `plant-images` bucket
- Public URLs generated for plant images

## Repository Classes

- `AuthRepository`: Handles user authentication and profile management
- `PlantRepository`: Manages plant data CRUD operations and image storage

## Testing the Integration

To verify Supabase is properly integrated:

1. Register a new user or log in with the test account
2. Add, update, or delete plants
3. Check your Supabase dashboard to confirm data and files are being saved correctly 