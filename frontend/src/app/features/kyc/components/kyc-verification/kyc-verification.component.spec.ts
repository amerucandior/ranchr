import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { KycVerificationComponent } from './kyc-verification.component';
import { KycService } from '../../../../core/services/kyc.service';
import { of } from 'rxjs';

describe('KycVerificationComponent', () => {
  let component: KycVerificationComponent;
  let kycService: KycService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KycVerificationComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    kycService = TestBed.inject(KycService);
    vi.spyOn(kycService, 'getKycStatus').mockReturnValue(
      of({ verified: false, phoneNumber: undefined, nationalId: undefined })
    );

    const fixture = TestBed.createComponent(KycVerificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the KYC verification component', () => {
    expect(component).toBeTruthy();
  });

  it('should validate phone number format for Kenyan prefix', () => {
    const phoneControl = component.phoneForm.get('phoneNumber');
    phoneControl?.setValue('0712345678'); // Missing +254
    expect(phoneControl?.valid).toBeFalsy();

    phoneControl?.setValue('+254712345678');
    expect(phoneControl?.valid).toBeTruthy();

    phoneControl?.setValue('+254112345678');
    expect(phoneControl?.valid).toBeTruthy();
  });

  it('should request OTP and advance to step 2 on valid submission', () => {
    const otpSpy = vi.spyOn(kycService, 'requestOtp').mockReturnValue(of('OTP sent'));

    component.phoneForm.patchValue({
      phoneNumber: '+254711223344',
      nationalId: '12345678',
    });

    component.sendOtp();

    expect(otpSpy).toHaveBeenCalledWith({
      phoneNumber: '+254711223344',
      nationalId: '12345678',
    });
    expect(component.otpStep()).toBe(2);
  });
});
