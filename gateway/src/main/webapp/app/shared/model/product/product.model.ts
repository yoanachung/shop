import { SalesStatus } from '@/shared/model/enumerations/sales-status.model';
export interface IProduct {
  id?: number;
  name?: string;
  description?: string | null;
  price?: number;
  remainingCount?: number | null;
  status?: SalesStatus;
}

export class Product implements IProduct {
  constructor(
    public id?: number,
    public name?: string,
    public description?: string | null,
    public price?: number,
    public remainingCount?: number | null,
    public status?: SalesStatus
  ) {}
}
