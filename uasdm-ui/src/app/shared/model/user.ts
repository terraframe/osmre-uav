///
///
///

import { LocalizedValue } from "./organization";

export class User {
  userName: string;
  loggedIn: boolean;
  externalProfile: boolean;
  roles: string[];
	organization?: { code: string, label: LocalizedValue };
}