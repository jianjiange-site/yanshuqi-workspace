-- USER-01：创建用户核心表索引

-- users 普通索引
CREATE INDEX idx_users_account_status ON user_center.users (account_status);
CREATE INDEX idx_users_user_type ON user_center.users (user_type);
CREATE INDEX idx_users_profile_status ON user_center.users (profile_status);

-- user_profiles 普通索引
CREATE INDEX idx_user_profiles_gender ON user_center.user_profiles (gender);
CREATE INDEX idx_user_profiles_country_city ON user_center.user_profiles (country_code, city_code);
CREATE INDEX idx_user_profiles_completed ON user_center.user_profiles (profile_completed);

-- user_photos 普通索引
CREATE INDEX idx_user_photos_user_type ON user_center.user_photos (user_id, photo_type);
CREATE INDEX idx_user_photos_review ON user_center.user_photos (review_status);
