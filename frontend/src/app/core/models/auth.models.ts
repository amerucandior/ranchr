export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface DecodedJwtPayload {
  sub: string;
  roles?: string[];
  exp?: number;
  iat?: number;
  [key: string]: unknown;
}

export interface CurrentUserSession {
  username: string;
  roles: string[];
  isVet: boolean;
  isFarmer: boolean;
}
