import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ApiError } from '../../../core/models/auth.models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required,
      Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{8,255}$/)]]
  });

  serverErrors: { [key: string]: string } = {};
  errorMessage = '';
  loading = false;

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.serverErrors = {};
    this.errorMessage = '';

    this.auth.register(this.form.value as any).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { registered: true } }),
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        const body: ApiError = err.error;
        if (err.status === 400 && body.errors) {
          body.errors.forEach(e => this.serverErrors[e.field] = e.message);
        } else {
          this.errorMessage = body?.title || 'Registration failed';
        }
      },
      complete: () => this.loading = false
    });
  }

  get name() { return this.form.get('name')!; }
  get email() { return this.form.get('email')!; }
  get password() { return this.form.get('password')!; }
}
