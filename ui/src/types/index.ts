export interface Album {
  id: string;
  name: string;
  description: string;
}

export interface Image {
  id: string;
  name: string;
  url: string;
  mediaType: string;
  size: number;
}

export interface PageResult<T> {
  page: number;
  size: number;
  totalCount: number;
  totalPages: number;
  list: T[];
}