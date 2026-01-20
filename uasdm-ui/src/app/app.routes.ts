///
///
///

import { Routes } from '@angular/router';

import { LoginComponent } from './core/component/login/login.component'
import { ForgotPasswordComponent } from './core/component/forgotpassword/forgotpassword.component'
import { ForgotPasswordCompleteComponent } from './core/component/forgotpassword-complete/forgotpassword-complete.component'

export const routes: Routes = [
    {
        path: 'menu',
        redirectTo: '/site/viewer',
    },
    {
        path: 'login',
        component: LoginComponent,
        data: { title: 'login.title' }
    },
    {
        path: 'login/:errorMsg',
        component: LoginComponent,
        data: { title: 'login.title' }
    },
    {
        path: 'forgotpassword',
        component: ForgotPasswordComponent,
        data: { title: 'useraccounts.title' }
    },
    {
        path: 'forgotpassword-complete/:token',
        component: ForgotPasswordCompleteComponent
    },
    {
        path: "admin",
        loadChildren: () => import('./admin/admin.routes')
    },
    {
        path: "site",
        loadChildren: () => import('./site/site.routes')
    },
    {
        path: '**',
        redirectTo: '/site/viewer',
    }
];