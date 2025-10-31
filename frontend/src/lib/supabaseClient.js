import { createClient } from '@supabase/supabase-js';

const supabaseUrl = process.env.VUE_APP_SUPABASE_URL;
const supabaseAnonKey = process.env.VUE_APP_SUPABASE_ANON_KEY;
// eslint-disable-next-line import/prefer-default-export
export const supabase = createClient(supabaseUrl, supabaseAnonKey);
