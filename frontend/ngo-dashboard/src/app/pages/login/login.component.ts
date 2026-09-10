import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = '';
  password = '';
  errorMessage: string | null = null;
  isSubmitting = false;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  onSubmit(): void {
    this.errorMessage = null;

    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Username and password are required.';
      return;
    }

    this.isSubmitting = true;
    this.authService.login(this.username.trim(), this.password).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigateByUrl('/matches');
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err?.error?.message ?? 'Login failed. Please try again.';
      }
    });
  }
}
