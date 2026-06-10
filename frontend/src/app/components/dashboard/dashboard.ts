import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs/operators';
import { PokemonService, PokemonDetail } from '../../services/pokemon';
import { UserService } from '../../services/user';
import { AuthService } from '../../services/auth';
import { LoadingComponent } from '../loading/loading';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, LoadingComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardComponent implements OnInit, OnDestroy {
  // Normal browsing
  pokemons: PokemonDetail[] = [];
  page = 0;
  totalCount = 0;
  loading = true;

  // Search mode
  searchResults: PokemonDetail[] = [];
  searchPage = 0;
  searchTotal = 0;
  isSearchMode = false;
  searching = false;

  // Shared
  searchQuery = '';
  favoritePokemon: PokemonDetail | null = null;
  favoritePokemonId: number | null = null;
  size = 20;

  private searchSubject = new Subject<string>();

  constructor(
    private pokemonService: PokemonService,
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.userService.getMe().subscribe({
      next: (user) => {
        this.favoritePokemonId = user.favoritePokemonId;
        if (user.favoritePokemonId) {
          this.pokemonService.getDetail(user.favoritePokemonId).subscribe({
            next: (pokemon) => { this.favoritePokemon = pokemon; this.cdr.detectChanges(); },
          });
        }
      },
    });

    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      filter((q) => q.length === 0 || q.length >= 3),
    ).subscribe((q) => {
      if (q.length === 0) {
        this.exitSearchMode();
      } else {
        this.searchPage = 0;
        this.runSearch(q);
      }
    });

    this.loadPokemons();
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }

  get displayedPokemons(): PokemonDetail[] {
    return this.isSearchMode ? this.searchResults : this.pokemons;
  }

  get currentPage(): number {
    return this.isSearchMode ? this.searchPage : this.page;
  }

  get isFirstPage(): boolean {
    return this.currentPage === 0;
  }

  get isLastPage(): boolean {
    const total = this.isSearchMode ? this.searchTotal : this.totalCount;
    return (this.currentPage + 1) * this.size >= total;
  }

  onSearch(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchQuery = value;
    this.searchSubject.next(value.trim());
  }

  private runSearch(q: string): void {
    this.searching = true;
    this.isSearchMode = true;
    this.pokemonService.search(q, this.searchPage, this.size).subscribe({
      next: (response) => {
        this.searchResults = response.results;
        this.searchTotal = response.count;
        this.searching = false;
        this.cdr.detectChanges();
      },
    });
  }

  private exitSearchMode(): void {
    this.isSearchMode = false;
    this.searchResults = [];
    this.searchTotal = 0;
    this.searchPage = 0;
    this.cdr.detectChanges();
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

  toggleFavorite(pokemon: PokemonDetail): void {
    if (this.favoritePokemonId === pokemon.id) {
      this.userService.removeFavorite().subscribe({
        next: () => { this.favoritePokemonId = null; this.favoritePokemon = null; this.cdr.detectChanges(); },
      });
    } else {
      this.userService.setFavorite(pokemon.id).subscribe({
        next: () => { this.favoritePokemonId = pokemon.id; this.favoritePokemon = pokemon; this.cdr.detectChanges(); },
      });
    }
  }

  nextPage(): void {
    if (this.isSearchMode) {
      this.searchPage++;
      this.runSearch(this.searchQuery.trim());
    } else {
      this.page++;
      this.loadPokemons();
    }
  }

  prevPage(): void {
    if (this.isSearchMode) {
      if (this.searchPage > 0) { this.searchPage--; this.runSearch(this.searchQuery.trim()); }
    } else {
      if (this.page > 0) { this.page--; this.loadPokemons(); }
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getTypeColor(type: string): string {
    const colors: Record<string, string> = {
      fire: '#F08030', water: '#6890F0', grass: '#78C850', electric: '#F8D030',
      psychic: '#F85888', ice: '#98D8D8', dragon: '#7038F8', dark: '#705848',
      fairy: '#EE99AC', normal: '#A8A878', fighting: '#C03028', flying: '#A890F0',
      poison: '#A040A0', ground: '#E0C068', rock: '#B8A038', bug: '#A8B820',
      ghost: '#705898', steel: '#B8B8D0',
    };
    return colors[type] || '#A8A878';
  }
}