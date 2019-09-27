export class PageResult<T> {
    count: number;
    pageNumber: number;
    pageSize: number;
    resultSet: T[];
}