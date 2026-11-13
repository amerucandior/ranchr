import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ListingService } from '../../services/listing.service';
import { ListingResponse, ListingServiceType } from '../../models/listing.models';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-marketplace-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="arva-page">
      <!-- Chapter 1: Scenic Promise Opening (10px white frame, 18px corners) -->
      <section class="scenic-stage-wrapper">
        <div class="scenic-stage">
          <div class="scenic-backdrop"></div>
          <div class="scenic-overlay"></div>

          <div class="scenic-content">
            <div class="scenic-tag">
              <span class="tag-marker"></span>
              EAST AFRICAN FIELDWORK MEETS GENETIC INTEGRITY
            </div>

            <h1 class="scenic-headline">
              Pastoral genetics cultivated<br>
              <span class="scenic-emphasis">for the East African herd.</span>
            </h1>

            <p class="scenic-support">
              Verified semen straws, zero-drop heat synchronization, and Kenya Veterinary Board (KVB) registered field practitioners at your pasture gate.
            </p>

            <div class="scenic-actions">
              <a routerLink="/dispatch" class="arva-capsule-primary urgent-action">
                <span class="pulse-indicator-inline"></span>
                <span>REQUEST FIELD DISPATCH</span>
                <span class="btn-arrow-disc lime-disc">→</span>
              </a>
              <a href="#catalog-section" class="arva-capsule-secondary">
                <span>EXPLORE BREEDING REGISTER</span>
              </a>
            </div>

            <div class="scenic-scroll-cue">
              <span class="scroll-dot"></span>
              <span class="scroll-label">FIELDWORK EVIDENCE BELOW</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Chapter 2: Alternating Benefit Panels (30px radius, 18px gap) -->
      <section class="benefits-chapter">
        <div class="chapter-container">
          <!-- Row 1: Dairy & Beef Breeders (Forest Green Panel) -->
          <div class="benefit-row">
            <div class="benefit-photo-frame">
              <img
                src="https://images.unsplash.com/photo-1546445317-29f4545e9d53?auto=format&fit=crop&w=900&q=80"
                alt="High-yield dairy cattle in East African pasture"
                class="benefit-photo" />
            </div>

            <div class="benefit-panel panel-forest">
              <span class="panel-audience">OUTCOME FOCUS</span>
              <h2 class="panel-title">For Dairy & Beef Breeders</h2>
              <p class="panel-intro">
                Accelerate herd milk yields and calf vigor with certified semen straw provenance, backed by a zero-drop 12-hour insemination guarantee.
              </p>

              <div class="benefit-grid-2x2">
                <div class="benefit-item">
                  <span class="benefit-icon">🧬</span>
                  <div class="benefit-item-content">
                    <h4>Traceable Straw Batches</h4>
                    <p>Every straw is verified by sire pedigree and official batch numbers.</p>
                  </div>
                </div>

                <div class="benefit-item">
                  <span class="benefit-icon">⚡</span>
                  <div class="benefit-item-content">
                    <h4>Zero-Drop Estrus Window</h4>
                    <p>Emergency dispatch alerts matching nearest available KVB officers.</p>
                  </div>
                </div>

                <div class="benefit-item">
                  <span class="benefit-icon">📅</span>
                  <div class="benefit-item-content">
                    <h4>21-Day Gestation Cycle</h4>
                    <p>Automated SMS timeline reminders for return-to-heat checks.</p>
                  </div>
                </div>

                <div class="benefit-item">
                  <span class="benefit-icon">🛡️</span>
                  <div class="benefit-item-content">
                    <h4>Verified KVB Officers</h4>
                    <p>All clinical care performed by licensed Kenyan veterinary professionals.</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Row 2: KVB Veterinary Officers (Pale Seed Cream Panel) -->
          <div class="benefit-row reverse">
            <div class="benefit-panel panel-cream">
              <span class="panel-audience audience-dark">PRACTITIONER EMPOWERMENT</span>
              <h2 class="panel-title title-dark">For KVB Veterinary Officers</h2>
              <p class="panel-intro intro-dark">
                A field-tested dispatch gateway that respects practitioner autonomy, routes nearby calls over SMS, and automates reproductive documentation.
              </p>

              <div class="benefit-grid-2x2">
                <div class="benefit-item item-dark">
                  <span class="benefit-icon">📲</span>
                  <div class="benefit-item-content">
                    <h4>Direct Paddock SMS Alerts</h4>
                    <p>Receive immediate alerts with farmer distance, urgency, and cow history.</p>
                  </div>
                </div>

                <div class="benefit-item item-dark">
                  <span class="benefit-icon">📶</span>
                  <div class="benefit-item-content">
                    <h4>Offline Two-Way Dispatch</h4>
                    <p>Accept calls and mark jobs complete using text commands to 20880.</p>
                  </div>
                </div>

                <div class="benefit-item item-dark">
                  <span class="benefit-icon">📋</span>
                  <div class="benefit-item-content">
                    <h4>Straw Traceability Logging</h4>
                    <p>Record semen straw serials directly into the automated herd ledger.</p>
                  </div>
                </div>

                <div class="benefit-item item-dark">
                  <span class="benefit-icon">⭐</span>
                  <div class="benefit-item-content">
                    <h4>Regional Practitioner Reputation</h4>
                    <p>Build 5-star ratings from verified breeders across your county.</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="benefit-photo-frame">
              <img
                src="https://images.unsplash.com/photo-1595974482597-4b8da8879bc5?auto=format&fit=crop&w=900&q=80"
                alt="Kenyan veterinary practitioner conducting field checkup"
                class="benefit-photo" />
            </div>
          </div>
        </div>
      </section>

      <!-- Chapter 3: Quantified Proof & Reach Drawing (Proof White #fdfdfd) -->
      <section class="proof-chapter">
        <div class="proof-container">
          <div class="proof-header">
            <span class="arva-impact-tag">MEASURED PROOF</span>
            <h2 class="proof-heading">Proven reach in the Kenyan paddock</h2>
            <p class="proof-sub">Field telemetry recorded across smallholder dairy cooperatives.</p>
          </div>

          <!-- Thin Oversized Numerals -->
          <div class="metric-row">
            <div class="metric-col">
              <div class="metric-number">1,240<span class="metric-plus">+</span></div>
              <div class="metric-label">Certified Semen Straws Tracked</div>
            </div>

            <div class="metric-col">
              <div class="metric-number">&lt; 12<span class="metric-unit">h</span></div>
              <div class="metric-label">Average Estrus Response Window</div>
            </div>

            <div class="metric-col">
              <div class="metric-number">47</div>
              <div class="metric-label">Counties Reachable via SMS 20880</div>
            </div>

            <div class="metric-col">
              <div class="metric-number">100<span class="metric-unit">%</span></div>
              <div class="metric-label">KVB Registered Practitioners</div>
            </div>
          </div>

          <!-- Fine-line Relationship Workflow Graphic -->
          <div class="reach-diagram-frame">
            <svg class="reach-diagram" viewBox="0 0 920 180" fill="none" xmlns="http://www.w3.org/2000/svg">
              <!-- Connecting path -->
              <path d="M 100 90 L 300 90 L 500 90 L 700 90 L 820 90" stroke="#c3cda7" stroke-width="1.5" stroke-dasharray="6 4" />

              <!-- Step 1 -->
              <circle cx="100" cy="90" r="32" fill="#ffffff" stroke="#07503f" stroke-width="2" />
              <text x="100" y="96" text-anchor="middle" font-size="20">🧑‍🌾</text>
              <text x="100" y="145" text-anchor="middle" font-size="13" font-family="'Inter', sans-serif" fill="#212529" font-weight="600">Farmer Heat Alert</text>
              <text x="100" y="162" text-anchor="middle" font-size="11" font-family="'Inter', sans-serif" fill="#6d6d6d">Web or SMS 20880</text>

              <!-- Step 2 -->
              <circle cx="300" cy="90" r="32" fill="#e8fe85" stroke="#07503f" stroke-width="2" />
              <text x="300" y="96" text-anchor="middle" font-size="20">⚡</text>
              <text x="300" y="145" text-anchor="middle" font-size="13" font-family="'Inter', sans-serif" fill="#212529" font-weight="600">Priority Engine</text>
              <text x="300" y="162" text-anchor="middle" font-size="11" font-family="'Inter', sans-serif" fill="#6d6d6d">&lt; 15s Match</text>

              <!-- Step 3 -->
              <circle cx="500" cy="90" r="32" fill="#ffffff" stroke="#07503f" stroke-width="2" />
              <text x="500" y="96" text-anchor="middle" font-size="20">👨‍⚕️</text>
              <text x="500" y="145" text-anchor="middle" font-size="13" font-family="'Inter', sans-serif" fill="#212529" font-weight="600">KVB Officer Dispatched</text>
              <text x="500" y="162" text-anchor="middle" font-size="11" font-family="'Inter', sans-serif" fill="#6d6d6d">Direct Mobile Arrival</text>

              <!-- Step 4 -->
              <circle cx="700" cy="90" r="32" fill="#ffffff" stroke="#07503f" stroke-width="2" />
              <text x="700" y="96" text-anchor="middle" font-size="20">🧬</text>
              <text x="700" y="145" text-anchor="middle" font-size="13" font-family="'Inter', sans-serif" fill="#212529" font-weight="600">Straw Batch Logged</text>
              <text x="700" y="162" text-anchor="middle" font-size="11" font-family="'Inter', sans-serif" fill="#6d6d6d">Genetic Traceability</text>

              <!-- Step 5 -->
              <circle cx="820" cy="90" r="32" fill="#b2cee7" stroke="#07503f" stroke-width="2" />
              <text x="820" y="96" text-anchor="middle" font-size="20">📅</text>
              <text x="820" y="145" text-anchor="middle" font-size="13" font-family="'Inter', sans-serif" fill="#212529" font-weight="600">Day 21 SMS</text>
              <text x="820" y="162" text-anchor="middle" font-size="11" font-family="'Inter', sans-serif" fill="#6d6d6d">Gestation Guard</text>
            </svg>
          </div>
        </div>
      </section>

      <!-- Chapter 4: Human Proof (Pastel Testimonials Pair) -->
      <section class="testimonial-chapter">
        <div class="chapter-container">
          <div class="testimonial-grid">
            <!-- Card 1: Sky Blue (#d7e6f5) -->
            <div class="testimonial-card card-blue">
              <div class="quote-text-side">
                <span class="quote-attribution">John Kiprop · Dairy Breeder, Nakuru</span>
                <p class="quote-body">
                  "When standing heat occurred at 3 AM in the paddock, <strong class="quote-bold">the registered KVB officer arrived with verified Friesian straws within 4 hours</strong>. The automated Day 21 alert helped us confirm pregnancy without guessing."
                </p>
                <div class="quote-tag">28L/DAY LACTATION OUTCOME</div>
              </div>
              <div class="quote-photo-side">
                <img
                  src="https://images.unsplash.com/photo-1500595046743-cd271d694d30?auto=format&fit=crop&w=600&q=80"
                  alt="John Kiprop at his dairy farm"
                  class="quote-photo" />
              </div>
            </div>

            <!-- Card 2: Green (#e4ebcf) -->
            <div class="testimonial-card card-green">
              <div class="quote-text-side">
                <span class="quote-attribution">Dr. David Kamau · KVB Officer #1048, Eldoret</span>
                <p class="quote-body">
                  "Logging semen straw serial numbers over two-way SMS <strong class="quote-bold">ensured every calf has documented pedigree and lineage integrity</strong> even when working far off the cellular data grid."
                </p>
                <div class="quote-tag">142 COMPLETED DISPATCHES</div>
              </div>
              <div class="quote-photo-side">
                <img
                  src="https://images.unsplash.com/photo-1622253692010-333f2da6031d?auto=format&fit=crop&w=600&q=80"
                  alt="Dr. David Kamau KVB Officer"
                  class="quote-photo" />
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Chapter 5: Editorial Livestock & Genetics Catalog (Oat Cream #f1efdf) -->
      <section id="catalog-section" class="catalog-chapter">
        <div class="chapter-container">
          <div class="catalog-header">
            <span class="arva-impact-tag">THE BREEDING REGISTER</span>
            <h2 class="catalog-title">Verified livestock & semen straws</h2>
            <p class="catalog-support">
              Inspect pedigreed breeding stock, artificial insemination straws, and regional natural service sires across Kenya.
            </p>
          </div>

          <!-- Search & Filter Controls -->
          <div class="editorial-toolbar">
            <div class="toolbar-search-row">
              <div class="search-field">
                <span class="search-icon">🔍</span>
                <input
                  type="text"
                  [(ngModel)]="searchKeyword"
                  (keyup.enter)="loadListings()"
                  placeholder="Filter by breed (e.g. Friesian, Ayrshire, Jersey, Boran)..." />
              </div>

              <div class="search-field">
                <span class="search-icon">📍</span>
                <input
                  type="text"
                  [(ngModel)]="searchLocation"
                  (keyup.enter)="loadListings()"
                  placeholder="County or town (e.g. Nakuru, Eldoret, Kiambu)..." />
              </div>

              <button (click)="loadListings()" class="btn-filter-apply">
                FILTER
              </button>
            </div>

            <div class="toolbar-chips">
              <button
                type="button"
                class="arva-chip"
                [class.active]="selectedServiceType === null"
                (click)="selectServiceType(null)">
                All Offerings
              </button>
              <button
                type="button"
                class="arva-chip"
                [class.active]="selectedServiceType === 'ARTIFICIAL_INSEMINATION'"
                (click)="selectServiceType('ARTIFICIAL_INSEMINATION')">
                AI Semen Straws
              </button>
              <button
                type="button"
                class="arva-chip"
                [class.active]="selectedServiceType === 'NATURAL'"
                (click)="selectServiceType('NATURAL')">
                Natural Sires
              </button>
              <button
                type="button"
                class="arva-chip"
                [class.active]="selectedServiceType === 'BOTH'"
                (click)="selectServiceType('BOTH')">
                Dual Service
              </button>

              <a routerLink="/marketplace/new" class="btn-list-animal-capsule">
                + LIST YOUR STOCK
              </a>
            </div>
          </div>

          <!-- Listings Editorial Grid (26px corners, serif titles) -->
          @if (isLoading()) {
            <div class="editorial-loading">
              <div class="arva-spinner"></div>
              <p>Consulting Kenyan livestock registry...</p>
            </div>
          } @else if (listings().length === 0) {
            <div class="editorial-empty">
              <span class="empty-mark">🌾</span>
              <h3>No livestock records match this search</h3>
              <p>Adjust your breed keyword or county location to discover active listings.</p>
              <button (click)="resetFilters()" class="btn-empty-reset">Reset Search</button>
            </div>
          } @else {
            <div class="editorial-grid-3up">
              @for (item of listings(); track item.id) {
                <article class="editorial-card">
                  <!-- Photograph with 26px corners -->
                  <div class="card-media-frame">
                    @if (item.images && item.images.length > 0) {
                      <img [src]="item.images[0].url" [alt]="item.breed || item.animalType" class="card-img" />
                    } @else {
                      <div class="card-placeholder">
                        <span class="symbol">{{ item.gender === 'MALE' ? '🐂' : '🐄' }}</span>
                      </div>
                    }

                    <span class="card-type-pill" [ngClass]="item.serviceType">
                      {{ item.serviceType === 'ARTIFICIAL_INSEMINATION' ? 'AI Straw' : item.serviceType }}
                    </span>
                  </div>

                  <!-- Editorial Text Content -->
                  <div class="card-meta">
                    <span class="meta-location">📍 {{ item.location }}</span>
                    @if (item.ageYears !== undefined && item.ageYears !== null) {
                      <span class="meta-age">· {{ item.ageYears }}y {{ item.ageMonths || 0 }}m</span>
                    }
                  </div>

                  <h3 class="card-title">{{ item.breed || item.animalType }}</h3>

                  <p class="card-excerpt">
                    {{ item.description || 'Verified livestock listing available for inspection and certified breeding service.' }}
                  </p>

                  <div class="card-bottom-row">
                    <div class="card-price-block">
                      <span class="price-caption">BREEDING FEE</span>
                      <span class="price-val">KES {{ item.fee | number }}</span>
                    </div>

                    <div class="card-actions">
                      <button
                        type="button"
                        class="btn-card-interest"
                        title="Express interest"
                        (click)="expressInterest(item)">
                        ❤️ {{ item.interestCount }}
                      </button>
                      @if (item.serviceType === 'ARTIFICIAL_INSEMINATION' || item.serviceType === 'BOTH') {
                        <a
                          [routerLink]="['/dispatch']"
                          [queryParams]="{ listingId: item.id, breed: item.breed || item.animalType, fee: item.fee, location: item.location }"
                          class="btn-card-dispatch"
                          title="Dispatch KVB vet for this straw">
                          ⚡ Dispatch AI
                        </a>
                      }
                      <a [routerLink]="['/connections']" [queryParams]="{ listingId: item.id }" class="btn-card-connect">
                        Inquire →
                      </a>
                    </div>
                  </div>
                </article>
              }
            </div>
          }
        </div>
      </section>

      <!-- Chapter 6: Sky Blue Closing Invitation (45px top corners, 140px vertical) -->
      <section class="closing-invitation-band">
        <div class="closing-content">
          <span class="closing-pill">JOIN THE RESILIENCE REGISTER</span>
          <h2 class="closing-title">Elevate your herd genetics today.</h2>
          <p class="closing-support">
            Join over 1,200 East African breeders and registered KVB officers building sustainable dairy resilience.
          </p>

          <div class="closing-actions">
            <a routerLink="/dispatch" class="arva-capsule-primary">
              <span>REQUEST FIELD DISPATCH NOW</span>
              <span class="btn-arrow-disc">→</span>
            </a>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .arva-page {
      background: var(--color-canvas);
      color: var(--color-ink);
    }

    /* Chapter 1: Scenic Promise Opening (10px white frame, 18px radius) */
    .scenic-stage-wrapper {
      padding: 10px 10px 30px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .scenic-stage {
      position: relative;
      border-radius: 18px;
      overflow: hidden;
      min-height: 640px;
      display: flex;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 80px 24px;
      color: #ffffff;
      box-shadow: 0 12px 36px rgba(7, 80, 63, 0.12);
    }

    .scenic-backdrop {
      position: absolute;
      inset: 0;
      background-image: url('https://images.unsplash.com/photo-1500595046743-cd271d694d30?auto=format&fit=crop&w=1800&q=85');
      background-size: cover;
      background-position: center 35%;
      z-index: 1;
    }

    .scenic-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(
        180deg,
        rgba(7, 80, 63, 0.65) 0%,
        rgba(7, 80, 63, 0.45) 40%,
        rgba(15, 23, 42, 0.85) 100%
      );
      z-index: 2;
    }

    .scenic-content {
      position: relative;
      z-index: 3;
      max-width: 860px;
      margin: 0 auto;
    }

    .scenic-tag {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: rgba(255, 255, 255, 0.12);
      border: 1px solid rgba(255, 255, 255, 0.25);
      border-radius: 110px;
      padding: 6px 16px;
      font-family: var(--font-sans);
      font-size: 11.5px;
      font-weight: 500;
      letter-spacing: 0.08em;
      color: var(--color-lime);
      margin-bottom: 24px;
      backdrop-filter: blur(4px);

      .tag-marker {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: var(--color-lime);
        box-shadow: 0 0 6px var(--color-lime);
      }
    }

    .scenic-headline {
      font-family: var(--font-serif);
      font-weight: 300;
      font-size: clamp(38px, 5.2vw, 65px);
      line-height: 1.06;
      margin: 0 0 20px 0;
      letter-spacing: -0.01em;
      color: #ffffff;

      .scenic-emphasis {
        font-weight: 400;
        font-style: italic;
        color: var(--color-lime);
      }
    }

    .scenic-support {
      font-family: var(--font-sans);
      font-weight: 300;
      font-size: clamp(15px, 1.8vw, 17px);
      line-height: 1.45;
      color: rgba(255, 255, 255, 0.88);
      max-width: 680px;
      margin: 0 auto 36px;
    }

    .scenic-actions {
      display: flex;
      justify-content: center;
      gap: 20px;
      flex-wrap: wrap;
      margin-bottom: 45px;

      .urgent-action {
        border: 1.5px solid var(--color-lime);
        box-shadow: 0 0 24px rgba(232, 254, 133, 0.3);

        .pulse-indicator-inline {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: var(--color-lime);
          box-shadow: 0 0 8px var(--color-lime);
          margin-right: 10px;
          animation: arva-pulse 1.5s infinite;
        }

        .lime-disc {
          background: var(--color-lime);
          color: var(--color-forest);
          font-weight: 700;
        }
      }
    }

    .scenic-scroll-cue {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-size: 11px;
      letter-spacing: 0.07em;
      color: rgba(255, 255, 255, 0.6);

      .scroll-dot {
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background: var(--color-lime);
      }
    }

    /* Chapter 2: Alternating Benefit Panels */
    .benefits-chapter {
      padding: 90px 24px;
      background: var(--color-canvas);
    }

    .chapter-container {
      max-width: 1350px;
      margin: 0 auto;
    }

    .benefit-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 18px;
      align-items: stretch;
      margin-bottom: 30px;

      @media (max-width: 960px) {
        grid-template-columns: 1fr;
      }

      &.reverse {
        @media (min-width: 961px) {
          grid-template-columns: 1fr 1fr;
        }
      }
    }

    .benefit-photo-frame {
      border-radius: 30px;
      overflow: hidden;
      min-height: 440px;

      .benefit-photo {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
    }

    .benefit-panel {
      border-radius: 30px;
      padding: 60px 48px;
      display: flex;
      flex-direction: column;
      justify-content: center;

      @media (max-width: 640px) {
        padding: 40px 24px;
      }

      &.panel-forest {
        background: var(--color-forest);
        color: #ffffff;
      }

      &.panel-cream {
        background: var(--color-benefit-cream);
        color: var(--color-ink);
      }

      .panel-audience {
        font-family: var(--font-sans);
        font-size: 12px;
        font-weight: 600;
        letter-spacing: 0.08em;
        color: var(--color-lime);
        margin-bottom: 12px;

        &.audience-dark {
          color: var(--color-forest);
        }
      }

      .panel-title {
        font-family: var(--font-serif);
        font-weight: 300;
        font-size: clamp(32px, 3.5vw, 45px);
        line-height: 1;
        margin: 0 0 16px 0;
        color: #ffffff;

        &.title-dark {
          color: var(--color-forest);
        }
      }

      .panel-intro {
        font-family: var(--font-sans);
        font-weight: 300;
        font-size: 16px;
        line-height: 1.45;
        color: rgba(255, 255, 255, 0.85);
        margin: 0 0 32px 0;
        max-width: 480px;

        &.intro-dark {
          color: var(--color-ink);
        }
      }

      .benefit-grid-2x2 {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 24px;

        @media (max-width: 600px) {
          grid-template-columns: 1fr;
        }

        .benefit-item {
          display: flex;
          gap: 12px;

          .benefit-icon {
            font-size: 22px;
            flex-shrink: 0;
          }

          .benefit-item-content {
            h4 {
              margin: 0 0 4px 0;
              font-family: var(--font-sans);
              font-size: 15px;
              font-weight: 600;
              color: #ffffff;
            }
            p {
              margin: 0;
              font-family: var(--font-sans);
              font-size: 13.5px;
              line-height: 1.4;
              color: rgba(255, 255, 255, 0.75);
            }
          }

          &.item-dark {
            .benefit-item-content {
              h4 {
                color: var(--color-deep-ink);
              }
              p {
                color: #555555;
              }
            }
          }
        }
      }
    }

    /* Chapter 3: Quantified Proof & Reach */
    .proof-chapter {
      background: var(--color-soft-white);
      padding: 100px 24px;
      border-top: 1px solid rgba(0, 0, 0, 0.05);
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    }

    .proof-container {
      max-width: 1160px;
      margin: 0 auto;
    }

    .proof-header {
      text-align: center;
      margin-bottom: 60px;

      .proof-heading {
        font-family: var(--font-serif);
        font-weight: 300;
        font-size: clamp(34px, 4vw, 52px);
        margin: 16px 0 10px;
        color: var(--color-forest);
      }

      .proof-sub {
        font-size: 16px;
        color: var(--color-muted);
        margin: 0;
      }
    }

    .metric-row {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
      margin-bottom: 60px;

      @media (max-width: 860px) {
        grid-template-columns: repeat(2, 1fr);
        gap: 30px;
      }
      @media (max-width: 480px) {
        grid-template-columns: 1fr;
      }

      .metric-col {
        border-left: 1px solid rgba(0, 0, 0, 0.1);
        padding-left: 24px;

        .metric-number {
          font-family: var(--font-sans);
          font-weight: 300;
          font-size: clamp(48px, 5.5vw, 76px);
          line-height: 1;
          color: var(--color-ink);
          letter-spacing: -0.04em;
          margin-bottom: 8px;

          .metric-plus, .metric-unit {
            font-weight: 200;
            color: var(--color-forest);
            font-size: 0.7em;
          }
        }

        .metric-label {
          font-family: var(--font-sans);
          font-size: 14px;
          color: var(--color-muted);
          line-height: 1.35;
        }
      }
    }

    .reach-diagram-frame {
      background: #ffffff;
      border: 1px solid rgba(0, 0, 0, 0.08);
      border-radius: 20px;
      padding: 30px;
      overflow-x: auto;

      .reach-diagram {
        width: 100%;
        min-width: 760px;
        height: auto;
      }
    }

    /* Chapter 4: Human Proof Testimonials */
    .testimonial-chapter {
      padding: 90px 24px;
      background: var(--color-canvas);
    }

    .testimonial-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 32px;

      @media (max-width: 960px) {
        grid-template-columns: 1fr;
      }

      .testimonial-card {
        border-radius: 15px;
        overflow: hidden;
        display: grid;
        grid-template-columns: 1fr 240px;
        min-height: 380px;

        @media (max-width: 600px) {
          grid-template-columns: 1fr;
        }

        &.card-blue {
          background: var(--color-quote-blue);
        }

        &.card-green {
          background: var(--color-quote-green);
        }

        .quote-text-side {
          padding: 40px 32px;
          display: flex;
          flex-direction: column;
          justify-content: space-between;

          .quote-attribution {
            font-family: var(--font-serif);
            font-size: 16px;
            font-weight: 500;
            color: var(--color-forest);
            margin-bottom: 16px;
          }

          .quote-body {
            font-family: var(--font-sans);
            font-size: 14.5px;
            line-height: 1.55;
            color: var(--color-ink);
            margin: 0 0 20px 0;

            .quote-bold {
              font-weight: 700;
              color: var(--color-forest);
            }
          }

          .quote-tag {
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.06em;
            color: var(--color-forest);
            background: rgba(255, 255, 255, 0.4);
            display: inline-block;
            padding: 4px 10px;
            border-radius: 6px;
            align-self: flex-start;
          }
        }

        .quote-photo-side {
          height: 100%;

          .quote-photo {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
          }
        }
      }
    }

    /* Chapter 5: Editorial Livestock & Genetics Catalog */
    .catalog-chapter {
      background: var(--color-cream);
      padding: 110px 24px;
    }

    .catalog-header {
      text-align: center;
      max-width: 760px;
      margin: 0 auto 50px;

      .catalog-title {
        font-family: var(--font-serif);
        font-weight: 300;
        font-size: clamp(36px, 4.5vw, 55px);
        line-height: 1;
        margin: 16px 0 12px;
        color: var(--color-forest);
      }

      .catalog-support {
        font-size: 17px;
        line-height: 1.45;
        color: #555555;
      }
    }

    .editorial-toolbar {
      background: #ffffff;
      border: 1px solid rgba(0, 0, 0, 0.08);
      border-radius: 20px;
      padding: 24px;
      margin-bottom: 50px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);

      .toolbar-search-row {
        display: grid;
        grid-template-columns: 2fr 1fr auto;
        gap: 12px;
        margin-bottom: 18px;

        @media (max-width: 768px) {
          grid-template-columns: 1fr;
        }

        .search-field {
          display: flex;
          align-items: center;
          background: var(--color-mist);
          border: 1px solid rgba(0, 0, 0, 0.08);
          border-radius: 110px;
          padding: 8px 18px;

          .search-icon {
            margin-right: 10px;
            font-size: 14px;
          }

          input {
            border: none;
            background: transparent;
            outline: none;
            width: 100%;
            font-size: 14.5px;
            color: var(--color-ink);
          }
        }

        .btn-filter-apply {
          background: var(--color-forest);
          color: #ffffff;
          border: none;
          border-radius: 110px;
          padding: 0 26px;
          font-weight: 600;
          font-size: 13.5px;
          letter-spacing: 0.04em;
          cursor: pointer;
          transition: background 0.3s;

          &:hover {
            background: #053b2e;
          }
        }
      }

      .toolbar-chips {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        align-items: center;

        .arva-chip {
          padding: 6px 16px;
          border-radius: 110px;
          border: 1px solid rgba(0, 0, 0, 0.12);
          background: var(--color-mist);
          font-size: 13px;
          font-weight: 500;
          color: var(--color-ink);
          cursor: pointer;
          transition: all 0.2s;

          &.active {
            background: var(--color-forest);
            color: #ffffff;
            border-color: var(--color-forest);
          }
        }

        .btn-list-animal-capsule {
          margin-left: auto;
          display: inline-flex;
          align-items: center;
          padding: 6px 18px;
          background: var(--color-forest);
          color: #ffffff;
          border-radius: 110px;
          font-size: 12.5px;
          font-weight: 600;
          text-decoration: none;
          letter-spacing: 0.04em;
          transition: all 0.2s;

          &:hover {
            background: var(--color-lime);
            color: var(--color-forest);
          }

          @media (max-width: 640px) {
            margin-left: 0;
            width: 100%;
            justify-content: center;
          }
        }
      }
    }

    .editorial-grid-3up {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 30px;

      @media (max-width: 992px) {
        grid-template-columns: repeat(2, 1fr);
      }
      @media (max-width: 640px) {
        grid-template-columns: 1fr;
      }
    }

    .editorial-card {
      display: flex;
      flex-direction: column;

      .card-media-frame {
        position: relative;
        height: 250px;
        border-radius: 26px;
        overflow: hidden;
        background: #e2e8f0;
        margin-bottom: 16px;

        .card-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
          transition: transform 0.4s ease;
        }

        &:hover .card-img {
          transform: scale(1.03);
        }

        .card-placeholder {
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #e9ecef;
          .symbol {
            font-size: 50px;
          }
        }

        .card-type-pill {
          position: absolute;
          top: 12px;
          left: 12px;
          background: rgba(255, 255, 255, 0.95);
          color: var(--color-forest);
          padding: 4px 10px;
          border-radius: 110px;
          font-size: 11.5px;
          font-weight: 600;
          letter-spacing: 0.04em;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);

          &.ARTIFICIAL_INSEMINATION {
            background: var(--color-forest);
            color: #ffffff;
          }
        }
      }

      .card-meta {
        font-size: 13.5px;
        color: var(--color-muted);
        margin-bottom: 6px;
      }

      .card-title {
        font-family: var(--font-serif);
        font-weight: 300;
        font-size: 25px;
        line-height: 1.22;
        color: var(--color-forest);
        margin: 0 0 10px 0;
      }

      .card-excerpt {
        font-size: 14.5px;
        line-height: 1.45;
        color: #555555;
        margin: 0 0 20px 0;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .card-bottom-row {
        margin-top: auto;
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        padding-top: 12px;
        border-top: 1px solid rgba(0, 0, 0, 0.08);

        .card-price-block {
          .price-caption {
            display: block;
            font-size: 11px;
            color: var(--color-muted);
            letter-spacing: 0.05em;
          }
          .price-val {
            font-family: var(--font-sans);
            font-weight: 700;
            font-size: 18px;
            color: var(--color-forest);
          }
        }

        .card-actions {
          display: flex;
          gap: 8px;
          align-items: center;

          .btn-card-interest {
            background: #ffffff;
            border: 1px solid rgba(0, 0, 0, 0.12);
            border-radius: 110px;
            padding: 6px 12px;
            font-size: 12.5px;
            cursor: pointer;
            transition: all 0.2s;

            &:hover {
              background: #fef2f2;
            }
          }

          .btn-card-dispatch {
            display: inline-flex;
            align-items: center;
            padding: 5px 12px;
            background: var(--color-forest);
            color: var(--color-lime);
            border-radius: 110px;
            font-size: 12px;
            font-weight: 600;
            text-decoration: none;
            letter-spacing: 0.03em;
            transition: all 0.2s;

            &:hover {
              background: #053b2e;
              color: #ffffff;
            }
          }

          .btn-card-connect {
            font-size: 13px;
            font-weight: 600;
            color: var(--color-forest);
            text-decoration: none;
            transition: color 0.2s;

            &:hover {
              color: var(--color-deep-ink);
              text-decoration: underline;
            }
          }
        }
      }
    }

    .editorial-loading, .editorial-empty {
      text-align: center;
      padding: 80px 20px;
      background: #ffffff;
      border-radius: 20px;

      .arva-spinner {
        width: 36px;
        height: 36px;
        border: 2px solid rgba(7, 80, 63, 0.15);
        border-top-color: var(--color-forest);
        border-radius: 50%;
        animation: arva-spin 0.8s linear infinite;
        margin: 0 auto 16px;
      }

      .empty-mark {
        font-size: 40px;
      }

      h3 {
        font-family: var(--font-serif);
        font-weight: 300;
        font-size: 24px;
        margin: 12px 0 6px;
        color: var(--color-forest);
      }

      p {
        color: var(--color-muted);
        font-size: 14.5px;
        margin: 0 0 16px;
      }

      .btn-empty-reset {
        background: var(--color-forest);
        color: #ffffff;
        border: none;
        border-radius: 110px;
        padding: 8px 22px;
        font-size: 13px;
        cursor: pointer;
      }
    }

    @keyframes arva-spin {
      to { transform: rotate(360deg); }
    }

    @keyframes arva-pulse {
      0% { transform: scale(0.9); opacity: 0.8; }
      50% { transform: scale(1.3); opacity: 1; }
      100% { transform: scale(0.9); opacity: 0.8; }
    }

    /* Chapter 6: Sky Blue Closing Invitation */
    .closing-invitation-band {
      background: var(--color-sky);
      border-radius: 45px 45px 0 0;
      padding: 130px 24px;
      text-align: center;
      color: var(--color-ink);

      .closing-content {
        max-width: 760px;
        margin: 0 auto;

        .closing-pill {
          display: inline-block;
          font-size: 12px;
          font-weight: 600;
          letter-spacing: 0.08em;
          color: var(--color-forest);
          background: rgba(255, 255, 255, 0.6);
          padding: 5px 16px;
          border-radius: 110px;
          margin-bottom: 24px;
        }

        .closing-title {
          font-family: var(--font-serif);
          font-weight: 300;
          font-size: clamp(36px, 4.8vw, 53px);
          line-height: 1;
          color: var(--color-forest);
          margin: 0 0 16px 0;
        }

        .closing-support {
          font-size: 17px;
          line-height: 1.45;
          color: #353535;
          margin: 0 auto 40px;
          max-width: 580px;
        }

        .closing-actions {
          display: flex;
          justify-content: center;
        }
      }
    }
  `],
})
export class MarketplaceCatalogComponent implements OnInit {
  private readonly listingService = inject(ListingService);
  readonly authService = inject(AuthService);

  readonly listings = signal<ListingResponse[]>([]);
  readonly isLoading = signal<boolean>(false);

  searchKeyword = '';
  searchLocation = '';
  selectedServiceType: ListingServiceType | null = null;

  ngOnInit(): void {
    this.loadListings();
  }

  loadListings(): void {
    this.isLoading.set(true);

    this.listingService
      .search({
        animalType: this.searchKeyword.trim() || undefined,
        location: this.searchLocation.trim() || undefined,
        serviceType: this.selectedServiceType || undefined,
      })
      .subscribe({
        next: (page) => {
          this.listings.set(page.content || []);
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
        },
      });
  }

  selectServiceType(type: ListingServiceType | null): void {
    this.selectedServiceType = type;
    this.loadListings();
  }

  resetFilters(): void {
    this.searchKeyword = '';
    this.searchLocation = '';
    this.selectedServiceType = null;
    this.loadListings();
  }

  expressInterest(item: ListingResponse): void {
    this.listingService.recordInterest(item.id).subscribe({
      next: () => {
        item.interestCount++;
      },
      error: () => {},
    });
  }
}
