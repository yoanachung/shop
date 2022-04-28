import { Component, Provide, Vue } from 'vue-property-decorator';

import UserService from '@/entities/user/user.service';
import ProductService from './product/product/product.service';
// jhipster-needle-add-entity-service-to-entities-component-import - JHipster will import entities services here

@Component
export default class Entities extends Vue {
  @Provide('userService') private userService = () => new UserService();
  @Provide('productService') private productService = () => new ProductService();
  // jhipster-needle-add-entity-service-to-entities-component - JHipster will import entities services here
}
