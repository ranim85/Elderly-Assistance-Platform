import { HttpContextToken } from '@angular/common/http';

/** Do not attach Bearer token (e.g. refresh call with expired access token). */
export const SKIP_AUTH = new HttpContextToken<boolean>(() => false);

/** Do not attempt token refresh on 401 (avoids refresh loops). */
export const SKIP_REFRESH_RETRY = new HttpContextToken<boolean>(() => false);
