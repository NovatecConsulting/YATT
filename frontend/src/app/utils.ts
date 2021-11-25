export function parseDate(isoDate: string): Date {
    const splitDate = isoDate.split("-");
    const year = parseInt(splitDate[0]);
    const month = parseInt(splitDate[1]);
    const day = parseInt(splitDate[2]);

    return new Date(year, month, day);
}