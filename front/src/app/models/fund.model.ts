export interface Fund {
  id: string;
  name: string;
  minimumAmount: number;
  category: 'FPV' | 'FIC';
}
