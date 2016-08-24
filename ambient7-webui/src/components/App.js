import React from 'react';

const App = () => {
  const opts = window.Ambient7Opts;
  return (
    <div>
      <header>
        <h1>Ambient7 WebUI</h1>
      </header>
      <section>
        {`api base: ${opts.apiBase}`}
      </section>
    </div>
  );
};

export default App;
