package com.example.HRMS.Backend.security;

import java.util.Collections;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public UserDetailsServiceImpl(@Lazy EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                "ROLE_" + employee.getFkRole().getRoleName().toUpperCase());

        System.out.println(authority);

        return new User(
                employee.getEmployeeEmail(),
                employee.getEmployeePassword(),
                employee.getEmployeeIsActive(),
                true,
                true,
                true,
                Collections.singletonList(authority));
    }
}
