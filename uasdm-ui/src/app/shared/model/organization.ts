///
///
///

import { PageResult } from "./page";

///

export class LocaleValue {

    locale: string;
    value: string;
}

export class LocalizedValue {

    localizedValue: string;
    localeValues: LocaleValue[];
}

export class Organization {

    code: string;
    label: LocalizedValue;
    contactInfo: LocalizedValue;
    parentCode?: string;
    parentLabel?: LocalizedValue
    enabled: boolean;
}

export class OrganizationNode {

    object: Organization;
    children: PageResult<OrganizationNode>;
}

export class OrganizationSync {
    oid?: string;
    url: string;
}