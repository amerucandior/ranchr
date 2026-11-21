import { Pipe, PipeTransform } from '@angular/core';

const LABELS: Record<string, string> = {
  PENDING: 'Waiting for a vet',
  ACCEPTED: 'Vet accepted',
  EN_ROUTE: 'Vet on the way',
  IN_PROGRESS: 'Service in progress',
  COMPLETED: 'Done',
  CANCELLED: 'Cancelled',
  INSEMINATED: 'Inseminated',
  HEAT_RETURNED: 'Back in heat',
  PREGNANCY_CONFIRMED: 'Pregnant',
  CALVED: 'Calved',
};

/** Turns backend enum statuses into plain-language labels. */
@Pipe({ name: 'statusLabel', standalone: true })
export class StatusLabelPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return '';
    return (
      LABELS[value] ??
      value.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase())
    );
  }
}
