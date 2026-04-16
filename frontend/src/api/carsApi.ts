import { axiosCars } from './axiosCars';

export interface Car {
  id: string;
  ownerId: string;
  marca: string;
  modelo: string;
  anio: number;
  placa: string;
  color: string;
  fotoUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CarInput {
  marca: string;
  modelo: string;
  anio: number;
  placa: string;
  color: string;
  fotoUrl?: string | null;
}

export interface CarListResponse {
  content: Car[];
  page: number;
  size: number;
  total: number;
}

export interface CarSearchParams {
  placa?: string;
  modelo?: string;
  marca?: string;
  anio?: number;
  page?: number;
  size?: number;
  sort?: string;
}

export const carsApi = {
  async list(params: CarSearchParams = {}): Promise<CarListResponse> {
    const { data } = await axiosCars.get<CarListResponse>('/cars', { params });
    return data;
  },
  async create(input: CarInput): Promise<Car> {
    const { data } = await axiosCars.post<Car>('/cars', input);
    return data;
  },
  async update(id: string, input: CarInput): Promise<Car> {
    const { data } = await axiosCars.put<Car>(`/cars/${id}`, input);
    return data;
  },
  async remove(id: string): Promise<void> {
    await axiosCars.delete(`/cars/${id}`);
  },
};
