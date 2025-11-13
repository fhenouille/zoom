import Home from '@/pages/Home';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

describe('Home Page', () => {
  it('should render welcome message', () => {
    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>
    );

    expect(screen.getByText(/Bienvenue sur Zoom Meetings/i)).toBeInTheDocument();
  });

  it('should render meetings button', () => {
    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>
    );

    expect(screen.getByText(/Voir les réunions/i)).toBeInTheDocument();
  });
});
