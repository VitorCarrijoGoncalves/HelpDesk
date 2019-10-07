package br.senaigo.helpdesk;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.senaigo.helpdesk.entity.User;
import br.senaigo.helpdesk.enums.ProfileEnum;
import br.senaigo.helpdesk.repository.UserRepository;

@SpringBootApplication
public class HelpDeskApplication extends SpringBootServletInitializer {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(HelpDeskApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			initUsers(userRepository, passwordEncoder);
		};
	}
	
	private void initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		User admin = new User();
		admin.setEmail("admin@helpdesk.com");
		admin.setPassword(passwordEncoder.encode("123456"));
		admin.setProfile(ProfileEnum.ROLE_ADMIN);
		
		User find = userRepository.findByEmail("admin@helpdesk.com");
		
		if (find == null) {
			userRepository.save(admin);
		}
		
	}
	
//	public WebMvcConfigurer corsConfigurer() {
//	    return new WebMvcConfigurer() {
//	        @Override
//	        public void addCorsMappings(CorsRegistry registry) {
//	            registry.addMapping("/login").allowedOrigins("http://localhost:4200");
//	        }
//
//			@Override
//			public void configurePathMatch(PathMatchConfigurer configurer) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void addFormatters(FormatterRegistry registry) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void addInterceptors(InterceptorRegistry registry) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void addResourceHandlers(ResourceHandlerRegistry registry) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void addViewControllers(ViewControllerRegistry registry) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void configureViewResolvers(ViewResolverRegistry registry) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public Validator getValidator() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public MessageCodesResolver getMessageCodesResolver() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//	    };
//	}

}