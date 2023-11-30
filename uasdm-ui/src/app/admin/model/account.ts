///
///
///

import { LocalizedValue } from "@shared/model/organization";

export class UserInvite {
	email: string;
	bureau: string;
	groups: Group[];
	bureaus: { value: string, label: string }[];
}

export class User {
	oid: string;
	username: string;
	password: string;
	firstName: string;
	lastName: string;
	email: string;
	phoneNumber: string;
	organization: { code: string, label: LocalizedValue };
	information: string;
	inactive: boolean;
	newInstance: boolean;
}

export class Role {
	roleId: string;
	displayLabel: string;
	assigned: boolean;
}

export class Group {
	name: string;
	roles: Role[];
}

export class Account {
	user: User;
	groups: Group[];
	changePassword?: boolean;
}