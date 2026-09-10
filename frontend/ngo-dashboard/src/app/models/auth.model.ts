export type Role = 'NGO';

export interface LoginRequest {
  username: string;
  password: string;
  role: Role;
}

export interface LoginResponse {
  token: string;
}
