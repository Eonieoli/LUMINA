// src/utils/analytics.ts
import ReactGA from 'react-ga4';

const GA_MEASUREMENT_ID = import.meta.env.VITE_GA_MEASUREMENT_ID; // GA4 측정 ID

export const initGA = () => {
  ReactGA.initialize(GA_MEASUREMENT_ID);
};

export const pageView = (path: string, title?: string) => {
  ReactGA.send({
    hitType: 'pageview',
    page: path,
    title: title,
  });
};

export const logEvent = (
  action: string,
  params?: Record<string, unknown>
) => {
  ReactGA.event(action, params);
};

export const logApiEvent = (
  apiName: string,
  status: 'success' | 'error',
  extra?: Record<string, unknown>
) => {
  logEvent('api_call', {
    api_name: apiName,
    status,
    ...extra,
  });
};