import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { pageView } from '../utils/analytics';

const GAListener: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();

  useEffect(() => {
    pageView(location.pathname + location.search);
  }, [location]);

  return <>{children}</>;
};

export default GAListener;
