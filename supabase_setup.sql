-- Run these SQL commands in your Supabase SQL Editor

-- Create user_profiles table
CREATE TABLE IF NOT EXISTS user_profiles (
  id UUID PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  user_type TEXT NOT NULL,
  nursery_name TEXT,
  owner_name TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create plants table
CREATE TABLE IF NOT EXISTS plants (
  id UUID PRIMARY KEY,
  owner_id UUID REFERENCES user_profiles(id),
  name TEXT NOT NULL,
  description TEXT,
  price DECIMAL(10, 2) NOT NULL,
  quantity INTEGER NOT NULL,
  care_instructions TEXT,
  image_url TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Set up Row Level Security (RLS) policies
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE plants ENABLE ROW LEVEL SECURITY;

-- User profiles can be read by anyone
CREATE POLICY user_profiles_select_policy ON user_profiles
  FOR SELECT USING (true);

-- User profiles can only be updated by the owner
CREATE POLICY user_profiles_update_policy ON user_profiles
  FOR UPDATE USING (auth.uid() = id);

-- Plants can be read by anyone
CREATE POLICY plants_select_policy ON plants
  FOR SELECT USING (true);

-- Plants can only be inserted/updated/deleted by the owner
CREATE POLICY plants_insert_policy ON plants
  FOR INSERT WITH CHECK (auth.uid() = owner_id);

CREATE POLICY plants_update_policy ON plants
  FOR UPDATE USING (auth.uid() = owner_id);

CREATE POLICY plants_delete_policy ON plants
  FOR DELETE USING (auth.uid() = owner_id); 