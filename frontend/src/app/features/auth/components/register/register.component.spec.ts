import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../../../core/services/auth.service';
import { of } from 'rxjs';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideRouter([{ path: 'kyc', component: RegisterComponent }]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  it('should create the register component', () => {
    expect(component).toBeTruthy();
  });

  it('should have invalid form initially', () => {
    expect(component.registerForm.valid).toBeFalsy();
  });

  it('should validate role defaults to USER and form becomes valid when filled', () => {
    expect(component.registerForm.get('role')?.value).toBe('USER');

    component.registerForm.patchValue({
      username: 'farmer_maina',
      email: 'maina@example.com',
      password: 'password123',
      role: 'VET',
    });

    expect(component.registerForm.valid).toBeTruthy();
    expect(component.registerForm.get('role')?.value).toBe('VET');
  });

  it('should invoke AuthService.register when form submitted', () => {
    const registerSpy = vi.spyOn(authService, 'register').mockReturnValue(
      of({ accessToken: 'test_acc', refreshToken: 'test_ref' })
    );

    component.registerForm.patchValue({
      username: 'dr_kamau',
      email: 'kamau@example.com',
      password: 'password123',
      role: 'VET',
    });

    component.submitRegister();

    expect(registerSpy).toHaveBeenCalledWith({
      username: 'dr_kamau',
      email: 'kamau@example.com',
      password: 'password123',
      role: 'VET',
    });
  });
});
