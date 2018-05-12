(function() {
  detectIE11();
  catchKeys();

  // submit form on CTRL+ENTER
  function catchKeys() {
    $(document).on('keydown', function(event) {
      if (event.ctrlKey && event.keyCode === 13) {
        let $target = $(event.target);
        // first let's try to sumbit parents form if it exist.
        let $form = $target.parents('form');
        if ($form.length === 0) {
          $form = $('form');
        }
        if ($form.length === 1) {
          let btn = $form.find('button[type=submit]');
          //workaround from $form.submit() to prevent submitting
          btn.click();
        }
      }
    });
  }

  function detectIE11() {
    if (!!navigator.userAgent.match(/Trident.*rv\:11\./)) {
      document.documentElement.className += ' ie11';
    }
  }
})();
