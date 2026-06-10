import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  favoritePokemonId: number | null;
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) {}

  getMe(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`);
  }

  setFavorite(pokemonId: number): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/favorite/${pokemonId}`, {});
  }

  removeFavorite(): Observable<UserProfile> {
    return this.http.delete<UserProfile>(`${this.apiUrl}/favorite`);
  }
}