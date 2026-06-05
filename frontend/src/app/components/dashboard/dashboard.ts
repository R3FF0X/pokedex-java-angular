import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { PokemonService, PokemonDetail } from '../../services/pokemon';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardComponent implements OnInit {
  pokemons: PokemonDetail[] = [];
  loading = true;
  page = 0;
  totalCount = 0;
  size = 20;

  constructor(
    private pokemonService: PokemonService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadPokemons();
  }

  loadPokemons(): void {
    this.loading = true;
    this.pokemonService.getList(this.page, this.size).subscribe({
      next: (response) => {
        this.totalCount = response.count;
        const requests = response.results.map((p) => {
          const id = p.url.split('/').filter(Boolean).pop()!;
          return this.pokemonService.getDetail(id);
        });

        let loaded = 0;
        this.pokemons = [];
        requests.forEach((req) => {
          req.subscribe((detail) => {
            this.pokemons.push(detail);
            loaded++;
            if (loaded === requests.length) {
              this.pokemons.sort((a, b) => a.id - b.id);
              this.loading = false;
              this.cdr.detectChanges();
            }
          });
        });
      },
    });
  }

  nextPage(): void {
    this.page++;
    this.loadPokemons();
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadPokemons();
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getTypeColor(type: string): string {
    const colors: Record<string, string> = {
      fire: '#F08030',
      water: '#6890F0',
      grass: '#78C850',
      electric: '#F8D030',
      psychic: '#F85888',
      ice: '#98D8D8',
      dragon: '#7038F8',
      dark: '#705848',
      fairy: '#EE99AC',
      normal: '#A8A878',
      fighting: '#C03028',
      flying: '#A890F0',
      poison: '#A040A0',
      ground: '#E0C068',
      rock: '#B8A038',
      bug: '#A8B820',
      ghost: '#705898',
      steel: '#B8B8D0',
    };
    return colors[type] || '#A8A878';
  }
}
