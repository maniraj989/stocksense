import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav-item.js';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import '@vaadin/scroller/theme/lumo/vaadin-scroller.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/login/theme/lumo/vaadin-login-form.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '5c954275eef55b14f7797a2029c3a2a6b3f94803e7e212230cb9f54879cec25f') {
    pending.push(import('./chunks/chunk-ae6ecfb81e9be316df9b67302918fe56b64fc8938251a8bd3cbb2b1127ba814f.js'));
  }
  if (key === '5dace30b0f7e4b6279ae145d28e34d8a44e5e7ce28fb6f582afff3f2765d86e3') {
    pending.push(import('./chunks/chunk-4e2a26bad26acdd803c6dcbbd52b47adcdbf0f5db0642489ba13fcb18ea04ba1.js'));
  }
  if (key === '324e0d62803d7a767d6b0195e42ce570f2bed6adccf49f4fbbeeb4bd830b8401') {
    pending.push(import('./chunks/chunk-0bf858f23052386728e7fcfa10ab4cc03aa89ab32f0a3fe0ad1ca8349152e7fa.js'));
  }
  if (key === '8dff523995d3a465a691a13e958828e0e02c298b90eed1bca1db085cc00b1133') {
    pending.push(import('./chunks/chunk-cfb4b9ab0cbf0da65193d30122d226a19d0543577f15133073080dd7919b3058.js'));
  }
  if (key === '0eb5ebb5fe81fd8421ae4d8c0007e086f36fb4c8f6603b1b246a45573973a989') {
    pending.push(import('./chunks/chunk-58e1f0a6cede618da77d61961dbc0377e0eb3b47d5f23091bf90833b1bbe7a1a.js'));
  }
  if (key === '8a9bfdb4da5386358815c53173ab164cf770bee33d1411470101bc53db4f712a') {
    pending.push(import('./chunks/chunk-84602bb3af267b26e1bbbb41c3e39990330a93d21ca169d6c8ef732593cfd0c3.js'));
  }
  if (key === '38e742e5609c75c25d57fd0a43757b88ac31fbe7aa60eeb68548139bc3d81e58') {
    pending.push(import('./chunks/chunk-fabe68ac7c7872cd3817d52a7de3a328913e7d5c9557dd3771f51ba23fdfa1ff.js'));
  }
  if (key === '53a2197c23d96af0f79260986ad5f4032b97e5a170a33ba842e839c1967aff82') {
    pending.push(import('./chunks/chunk-722f17a09f8d4e29e5614ea77474931b569a49a2ab28ba05599f0bded1abeca9.js'));
  }
  if (key === 'fb669a1027fedca101c3f3b2a7233ee70c667fb7e94b33b94d998ac929b0bdfb') {
    pending.push(import('./chunks/chunk-56acf93b83764160b4fca9a13c6628582f52366c2af8e26bd6878de3b88d4287.js'));
  }
  if (key === '44673e3fe175b3f5c1fa58bb2b03676dfa2ae6329b20717042e33c6b6ee25a67') {
    pending.push(import('./chunks/chunk-57c9ebd9c4ae5c56da22f6f8f0759e9231a798a3d747213e6c5f55a0eb3204bd.js'));
  }
  if (key === 'ea4fec5a55b57fa48280e41ec8f33d9000276377b507142c1b0d8c2b472e5470') {
    pending.push(import('./chunks/chunk-f3e7d3c0f504fb4b4380d30335adad6d6955117b5cd56aa1165dceb2d00d5341.js'));
  }
  if (key === '1d2ed85e558618513056f5e81068cad7c892f4a1cb2dc58a67ddfed0ccb26225') {
    pending.push(import('./chunks/chunk-0bf858f23052386728e7fcfa10ab4cc03aa89ab32f0a3fe0ad1ca8349152e7fa.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}