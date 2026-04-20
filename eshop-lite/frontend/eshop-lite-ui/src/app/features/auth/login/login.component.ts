import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  errorMessage = '';
  successMessage = '';
  loading = false;

  ngOnInit() {
    if (this.route.snapshot.queryParams['registered']) {
      this.successMessage = 'Account created! Please log in.';
    }
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMessage = '';

    this.auth.login(this.form.value as any).subscribe({
      next: () => this.router.navigate(['/profile']),
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.errorMessage = err.error?.title || 'Invalid credentials';
      },
      complete: () => this.loading = false
    });
  }

  get email() { return this.form.get('email')!; }
  get password() { return this.form.get('password')!; }
}
