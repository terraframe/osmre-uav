import { createActionGroup, props, createReducer, on, createFeatureSelector, createSelector, emptyProps } from "@ngrx/store";
// @ts-ignore
import { User } from "@shared/model/user";

export const SessionActions = createActionGroup({
    source: 'session',
    events: {
        'Set User': props<{ user: User | null }>(),
        'Remove User': emptyProps()
    },
});


export interface SessionStateModel {
    user: User | null
}

export const initialState: SessionStateModel = {
    user: {
        loggedIn: false,
        userName: '',
        externalProfile: false,
        roles: []
    }
}

export const sessionReducer = createReducer(
    initialState,

    // Set zones displayed on the map
    on(SessionActions.setUser, (state, { user }) => {

        return {
            ...state,
            user
        };
    }),

    on(SessionActions.removeUser, (state) => {

        return {
            ...state,
            user: {
                loggedIn: false,
                userName: '',
                externalProfile: false,
                roles: []
            }
        };
    }),
);


const selector = createFeatureSelector<SessionStateModel>('session');

export const getUser = createSelector(selector, (s) => {
    return s.user;
});