import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PokemonListItem {
  name: string;
  url: string;
}

export interface PokemonListResponse {
  count: number;
  next: string | null;
  previous: string | null;
  results: PokemonListItem[];
}

export interface PokemonDetail {
  id: number;
  name: string;
  height: number;
  weight: number;
  sprites: {
    front_default: string;
    other: {
      'official-artwork': {
        front_default: string;
      };
    };
  };
  types: {
    type: {
      name: string;
    };
  }[];
  stats: {
    base_stat: number;
    stat: {
      name: string;
    };
  }[];
}

@Injectable({
  providedIn: 'root',
})
export class PokemonService {
  private apiUrl = '/api/pokemon';

  constructor(private http: HttpClient) {}

  getList(page: number = 0, size: number = 20): Observable<PokemonListResponse> {
    return this.http.get<PokemonListResponse>(`${this.apiUrl}?page=${page}&size=${size}`);
  }

  getDetail(nameOrId: string | number): Observable<PokemonDetail> {
    return this.http.get<PokemonDetail>(`${this.apiUrl}/${nameOrId}`);
  }
}
