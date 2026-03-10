///
///
///

import { Component, OnInit, OnDestroy, Output, EventEmitter, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { v4 as uuid } from "uuid";

import { ManagementService } from '@site/service/management.service';
import { Criteria, Filter, StacItem, StacLayer } from '@site/model/layer';
import { PageResult } from '@shared/model/page';
import EnvironmentUtil from '@core/utility/environment-util';
import { environment } from 'src/environments/environment';
import { LngLatBounds } from 'maplibre-gl';
import { UasdmHeaderComponent } from '@shared/component/header/header.component';
import { LocalizedValue } from '@shared/model/organization';
import { HelpService, HelpPageContentResponse } from '@site/service/help-service';
import { MarkdownComponent } from 'ngx-markdown';
import { OrganizationFieldComponent } from '@shared/component/organization-field/organization-field.component';
import { AuthService } from '@shared/service/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CollapseModule } from 'ngx-bootstrap/collapse';

const enum VIEW_MODE {
    FORM = 0,
    RESULTS = 1
}

@Component({
    standalone: true,
    selector: 'help',
    templateUrl: './help.component.html',
    styleUrls: ['./help.component.scss'],
    imports: [CommonModule, FormsModule, CollapseModule, UasdmHeaderComponent, MarkdownComponent, OrganizationFieldComponent]
})
export class HelpComponent implements OnInit {

    public isBugReportCollapsed: boolean = true;
    public isFeatureCollapsed: boolean = true;
    public isQuestionCollapsed: boolean = true;

    reportEmail: string = "example@address.com";

    public data: HelpPageContentResponse = { markdown: "", organization: null };
    public renderedMarkdown = '';
    public showRenderedMarkdown: boolean = true;

    public isAdmin: boolean = false;

    public isEditing: boolean = false;
    public editMarkdown: string = '';
    public isSaving: boolean = false;

    public defaultMarkdownTemplate = `## [Bureau Name] Help Desk

- **Help Desk URL:** [Help Desk Portal](https://www.markdownguide.org/cheat-sheet)
- **Help Desk Email:** [helpdesk@example.gov](mailto:helpdesk@example.gov)
- **Help Desk Phone:** [Optional Help Desk Phone Number]
- **Support Hours:** [e.g. Monday-Friday, 8:00 AM-5:00 PM Mountain Time]
- **Preferred Contact Method:** [Portal / Email / Phone / Any]

## Help Desk Instructions

Please choose the support method that best matches your need:

- **Portal:** Use the help desk portal for the fastest tracking and resolution of tickets.
- **Email:** Use email for bug reports, feature requests, and general questions.
- **Phone:** Use phone support for urgent issues, if available.
- **Any:** If no preferred method is required, use whichever is most convenient.

Before submitting a request, please gather as much relevant information as possible, such as screenshots, URLs, error messages, and steps to reproduce the issue.

## Support Request Templates

Use the following templates when submitting requests to the help desk.

<details>
<summary>Bug Report Template</summary>

**Subject:**  
\`Bug Report: <short description of issue>\`

**Body:**

\`\`\`
Summary
- Briefly describe the issue in one sentence.

Steps to reproduce
1. Go to:
2. Perform action:
3. Expected result:
4. Actual result:

Expected behavior
- What should have happened?

Actual behavior
- What happened instead?
- Include error messages if applicable.

Environment
- Application URL or instance:
- Application version:
- OS:
- Browser and version:
- Network/VPN/proxy details (if relevant):

Scope and impact
- How often does the issue occur?
- Does it block your workflow? (yes/no)
- Who is affected?

Attachments
- Screenshots or screen recordings:
- Relevant logs or console output:

Additional context
- Any configuration details, unusual circumstances, or related issues.

Optional additional items
- Issue default title:
- Assignees:
- Labels:
\`\`\`

</details>

<details>
<summary>Feature Request Template</summary>

**Subject:**  
\`Feature Request: <short description of request>\`

**Body:**

\`\`\`
Issue: Feature request
Suggest an idea that would improve this project

Summary
- Briefly describe the feature you would like added.

User story
- As a [type of user], I want [feature], so that [benefit].

Problem / Job-to-be-done
- What problem does this feature solve?
- What workflow or goal is currently difficult or impossible?

Proposed solution
- Describe how you think the feature should work.
- Include specific behaviors, inputs, outputs, or UI expectations.

Why this solution?
- What makes this option valuable or necessary?
- Who benefits?

Considered alternatives
- Have you tried other approaches? Why did they not work?

Impact / Priority
- How important is this for your work? (e.g. low / medium / high)

Additional context
- Screenshots, URLs, or examples
- Related tickets, integrations, or dependencies

Optional additional items
- Issue default title:
- Assignees:
- Labels: enhancement
\`\`\`

</details>

<details>
<summary>General Question Template</summary>

**Subject:**  
\`Question: <short description of question>\`

**Body:**

\`\`\`
Issue: Question
Ask for clarification or guidance

Question
- What is your question?

Context
- Where in the application are you?
- What are you trying to accomplish?

Steps taken so far
1.
2.
3.

What you expected to happen
- Describe what you thought would happen

What actually happened (if applicable)
- Describe what you observed

Relevant details (if any)
- Screenshots or URLs:
- Environment (OS / browser / version):

Optional additional items
- Issue default title:
- Assignees:
- Labels:
\`\`\`

</details>

<br>

## Documentation

Below are bureau-specific documentation resources.

### Core Documentation

- **[Document Title 1](https://www.markdownguide.org/cheat-sheet)**  
  Short description of what this document covers and when users should reference it.

- **[Document Title 2](https://www.markdownguide.org/cheat-sheet)**  
  Short description of what this document covers and when users should reference it.

- **[Document Title 3](https://www.markdownguide.org/cheat-sheet)**  
  Short description of what this document covers and when users should reference it.

## Training Resources

Use the following training resources to learn more about bureau processes, tools, and workflows.

- **[Training Resource 1](https://www.markdownguide.org/cheat-sheet)**  
  Short description of the training content.

- **[Training Resource 2](https://www.markdownguide.org/cheat-sheet)**  
  Short description of the training content.

- **[Training Resource 3](https://www.markdownguide.org/cheat-sheet)**  
  Short description of the training content.

## Additional Notes

Add any bureau-specific policies, escalation guidance, onboarding notes, or special instructions here.
`;

    constructor(private helpService: HelpService, private authService: AuthService) { }

    ngOnInit(): void {
        this.isAdmin = this.authService.isAdmin();
        this.fetchData();
    }

    onOrganizationChange(): void {
        if (!this.data?.organization?.code)
            return;

        this.fetchData(true);
    }

    fetchData(andStartEdit: boolean = false) {
        let orgCode = this.data?.organization?.code;

        this.helpService.content(orgCode).subscribe(response => {
            this.data = response;
            this.renderedMarkdown = this.data.markdown;

            if (andStartEdit)
                this.startEdit();

            // this.showRenderedMarkdown = false;
            // setTimeout(() => {
            //     this.showRenderedMarkdown = true;
            // },0);
        });
    }

    startEdit(): void {
        this.editMarkdown = (this.data?.markdown == null || this.data?.markdown === '') ? this.defaultMarkdownTemplate : this.data?.markdown;
        this.isEditing = true;
    }

    cancelEdit(): void {
        this.editMarkdown = this.data?.markdown ?? '';
        this.isEditing = false;
    }

    saveEdit(): void {
        this.isSaving = true;

        this.helpService.edit(this.data?.organization?.code, this.editMarkdown).subscribe({
            next: () => {
                if (this.data) {
                    this.data = {
                        ...this.data,
                        markdown: this.editMarkdown
                    };
                }
                this.renderedMarkdown = this.editMarkdown;
                this.isEditing = false;
                this.isSaving = false;
            },
            error: (err) => {
                console.error('Failed to save help markdown', err);
                this.isSaving = false;
            }
        });
    }

    copy(text: string) {
        navigator.clipboard.writeText(text).then(() => {
            console.log("Copied:", text);
        }).catch(err => {
            console.error("Clipboard copy failed:", err);
        });
    }

}
