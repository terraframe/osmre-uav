///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';

import { User, PageResult } from './account';
import { AccountService } from './account.service';

declare let acp: string;

@Component({
  selector: 'accounts',
  templateUrl: './accounts.component.html',
  styles: ['./accounts.css']
})
export class AccountsComponent implements OnInit {
  res:PageResult = {
    resultSet:[],
    count:0,
    pageNumber:1,
    pageSize:10
  };
  p:number = 1; 
  
  constructor(private router: Router, private service: AccountService) { }

  ngOnInit(): void {
    this.service.page(this.p).then(res => {
      this.res = res;	
    });	  
  }
  
  remove(user:User) : void {
    this.service.remove(user.oid).then(response => {
      this.res.resultSet = this.res.resultSet.filter(h => h.oid !== user.oid);    
    });	  	  
  }
  
  edit(user:User) : void {
    this.router.navigate(['/admin/account', user.oid]);	  
  }
  
  newInstance(pageNumber:number): void {
    this.router.navigate(['/admin/account', 'NEW']);	  
  }  
  
  onPageChange(pageNumber:number): void {
    this.service.page(pageNumber).then(res => {
      this.res = res;	
    });	  	  
  }  
}
