import { render, screen } from '@testing-library/react';
import App from './App';

test('renders app shell', () => {
  render(<App />);
  const appShell = document.querySelector('.app-shell');
  expect(appShell).toBeInTheDocument();
});
