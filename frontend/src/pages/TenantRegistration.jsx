import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { Eye, EyeOff, Check, ArrowLeft, ArrowRight, Building, User, CreditCard } from "lucide-react";
import { toast } from "react-hot-toast";
import api from "../services/api";

const TenantRegistration = () => {
  const { registerTenant } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  const totalSteps = 3;

  const {
    register,
    handleSubmit,
    formState: { errors },
    trigger,
    getValues,
  } = useForm();

  const steps = [
    {
      id: 1,
      title: "Organization",
      icon: Building,
      description: "Company details"
    },
    {
      id: 2,
      title: "Admin Account",
      icon: User,
      description: "Administrator setup"
    },
    {
      id: 3,
      title: "Subscription",
      icon: CreditCard,
      description: "Choose your plan"
    }
  ];

  const validateStep = async (step) => {
    let fieldsToValidate = [];
    
    switch (step) {
      case 1:
        fieldsToValidate = ['tenantName', 'companyName'];
        break;
      case 2:
        fieldsToValidate = ['adminFirstName', 'adminLastName', 'adminEmail', 'adminUsername', 'adminPassword'];
        break;
      case 3:
        fieldsToValidate = ['acceptTerms'];
        break;
      default:
        return true;
    }
    
    const result = await trigger(fieldsToValidate);
    return result;
  };

  const nextStep = async () => {
    const isValid = await validateStep(currentStep);
    if (isValid && currentStep < totalSteps) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const onSubmit = async (data) => {
    setIsSubmitting(true);

    const registrationData = {
      tenantName: data.tenantName,
      companyName: data.companyName || data.tenantName,
      adminFirstName: data.adminFirstName,
      adminLastName: data.adminLastName,
      adminEmail: data.adminEmail,
      adminUsername: data.adminUsername || data.adminEmail.split("@")[0],
      adminPassword: data.adminPassword,
      companyAddress: data.companyAddress,
      companyPhone: data.companyPhone,
      companyEmail: data.companyEmail || data.adminEmail,
      industry: data.industry,
      timezone: data.timezone || "UTC",
      locale: data.locale || "en",
      subscriptionPlan: data.subscriptionPlan || "FREE",
      acceptTerms: true,
      acceptMarketing: data.acceptMarketing || false,
    };

    const result = await registerTenant(registrationData);
    setIsSubmitting(false);

    if (result.success) {
      navigate("/login");
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="grid lg:grid-cols-2 gap-12 items-start">
          {/* Left Section - Header and Information */}
          <div className="lg:sticky lg:top-8">
            <Link
              to="/"
              className="inline-flex items-center text-primary-600 hover:text-primary-700 mb-8 transition-colors duration-200"
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Home
            </Link>
            
            <div className="mb-8">
              <div className="w-20 h-20 bg-gradient-to-r from-primary-500 to-purple-600 rounded-3xl mb-6 flex items-center justify-center shadow-xl">
                <div className="w-10 h-10 bg-white rounded-xl"></div>
              </div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent mb-4 leading-tight">
                Create Your Organization
              </h1>
              <p className="text-lg text-gray-600 leading-relaxed">
                Set up your CRM workspace and start managing customer relationships with ease. Join thousands of businesses already using our platform.
              </p>
            </div>

            {/* Benefits */}
            <div className="space-y-4 mb-8">
              <div className="flex items-start space-x-3">
                <div className="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Check className="h-4 w-4 text-green-600" />
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-gray-900">Quick Setup</h3>
                  <p className="text-sm text-gray-600">Get started in minutes with our guided setup process</p>
                </div>
              </div>
              <div className="flex items-start space-x-3">
                <div className="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Check className="h-4 w-4 text-green-600" />
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-gray-900">Free Trial</h3>
                  <p className="text-sm text-gray-600">30-day free trial with full access to all features</p>
                </div>
              </div>
              <div className="flex items-start space-x-3">
                <div className="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Check className="h-4 w-4 text-green-600" />
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-gray-900">Secure & Reliable</h3>
                  <p className="text-sm text-gray-600">Enterprise-grade security with 99.9% uptime</p>
                </div>
              </div>
            </div>

          </div>

          {/* Right Section - Form */}
          <div className="lg:col-span-1">

            <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
              {/* Form Stepper */}
              <div className="px-8 pt-6 pb-4 border-b border-gray-100">
                <div className="flex items-center justify-between">
                  {steps.map((step, index) => {
                    const Icon = step.icon;
                    const isActive = currentStep === step.id;
                    const isCompleted = currentStep > step.id;
                    
                    return (
                      <div key={step.id} className="flex items-center">
                        <div className="flex flex-col items-center">
                          <div
                            className={`w-10 h-10 rounded-full flex items-center justify-center transition-all duration-300 ${
                              isCompleted
                                ? 'bg-green-500 text-white'
                                : isActive
                                ? 'bg-primary-600 text-white shadow-lg'
                                : 'bg-gray-200 text-gray-500'
                            }`}
                          >
                            {isCompleted ? (
                              <Check className="h-5 w-5" />
                            ) : (
                              <Icon className="h-5 w-5" />
                            )}
                          </div>
                          <div className="mt-2 text-center">
                            <div
                              className={`text-sm font-medium ${
                                isActive ? 'text-primary-600' : isCompleted ? 'text-green-600' : 'text-gray-500'
                              }`}
                            >
                              {step.title}
                            </div>
                            <div className="text-xs text-gray-400">{step.description}</div>
                          </div>
                        </div>
                        {index < steps.length - 1 && (
                          <div
                            className={`flex-1 h-0.5 mx-4 transition-all duration-300 ${
                              currentStep > step.id ? 'bg-green-500' : 'bg-gray-200'
                            }`}
                            style={{ minWidth: '40px' }}
                          />
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>

              <div className="overflow-hidden">
                <form onSubmit={handleSubmit(onSubmit)} className="p-8">
            {/* Step 1: Organization Information */}
            {currentStep === 1 && (
              <div className="space-y-6">
                <div className="text-center mb-8">
                  <Building className="h-12 w-12 text-primary-600 mx-auto mb-4" />
                  <h2 className="text-2xl font-bold text-gray-900 mb-2">Organization Information</h2>
                  <p className="text-gray-600">Tell us about your company</p>
                </div>

              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Organization Name *</label>
                  <input
                    type="text"
                    {...register("tenantName", {
                      required: "Organization name is required",
                      minLength: {
                        value: 2,
                        message: "Name must be at least 2 characters",
                      },
                    })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="Acme Corporation"
                  />
                  {errors.tenantName && (
                    <p className="text-sm text-red-600 flex items-center mt-1">
                      <span className="w-4 h-4 mr-1">⚠️</span>
                      {errors.tenantName.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Company Name *</label>
                  <input
                    type="text"
                    {...register("companyName", {
                      required: "Company name is required",
                      minLength: {
                        value: 2,
                        message: "Name must be at least 2 characters",
                      },
                    })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="Acme Corporation"
                  />
                  {errors.companyName && (
                    <p className="text-sm text-red-600 flex items-center mt-1">
                      <span className="w-4 h-4 mr-1">⚠️</span>
                      {errors.companyName.message}
                    </p>
                  )}
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-6 mt-6">
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Company Email</label>
                  <input
                    type="email"
                    {...register("companyEmail", {
                      pattern: {
                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                        message: "Invalid email format",
                      },
                    })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="info@acme.com"
                  />
                  {errors.companyEmail && (
                    <p className="text-sm text-red-600 flex items-center mt-1">
                      <span className="w-4 h-4 mr-1">⚠️</span>
                      {errors.companyEmail.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Company Phone</label>
                  <input
                    type="tel"
                    {...register("companyPhone")}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="+1-555-0123"
                  />
                </div>
              </div>

              <div className="mt-6 space-y-2">
                <label className="block text-sm font-medium text-gray-700">Company Address</label>
                <textarea
                  {...register("companyAddress")}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white resize-none"
                  rows="3"
                  placeholder="123 Business St, City, State, ZIP"
                />
              </div>

              <div className="grid md:grid-cols-2 gap-6 mt-6">
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Industry</label>
                  <select {...register("industry")} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white">
                    <option value="">Select Industry</option>
                    <option value="Technology">Technology</option>
                    <option value="Healthcare">Healthcare</option>
                    <option value="Finance">Finance</option>
                    <option value="Education">Education</option>
                    <option value="Retail">Retail</option>
                    <option value="Manufacturing">Manufacturing</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Timezone</label>
                  <select {...register("timezone")} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white">
                    <option value="UTC">UTC</option>
                    <option value="America/New_York">Eastern Time</option>
                    <option value="America/Chicago">Central Time</option>
                    <option value="America/Denver">Mountain Time</option>
                    <option value="America/Los_Angeles">Pacific Time</option>
                    <option value="Europe/London">London</option>
                    <option value="Europe/Paris">Paris</option>
                    <option value="Asia/Tokyo">Tokyo</option>
                  </select>
                </div>
              </div>
              </div>
            )}

            {/* Step 2: Admin User Information */}
            {currentStep === 2 && (
              <div className="space-y-6">
                <div className="text-center mb-8">
                  <User className="h-12 w-12 text-primary-600 mx-auto mb-4" />
                  <h2 className="text-2xl font-bold text-gray-900 mb-2">Admin Account</h2>
                  <p className="text-gray-600">Create your administrator account</p>
                </div>

              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">First Name *</label>
                  <input
                    type="text"
                    {...register("adminFirstName", {
                      required: "First name is required",
                      minLength: {
                        value: 2,
                        message: "Name must be at least 2 characters",
                      },
                    })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="John"
                  />
                  {errors.adminFirstName && (
                    <p className="text-sm text-red-600 flex items-center mt-1">
                      <span className="w-4 h-4 mr-1">⚠️</span>
                      {errors.adminFirstName.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Last Name *</label>
                  <input
                    type="text"
                    {...register("adminLastName", {
                      required: "Last name is required",
                      minLength: {
                        value: 2,
                        message: "Name must be at least 2 characters",
                      },
                    })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="Doe"
                  />
                  {errors.adminLastName && (
                    <p className="text-sm text-red-600 flex items-center mt-1">
                      <span className="w-4 h-4 mr-1">⚠️</span>
                      {errors.adminLastName.message}
                    </p>
                  )}
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-6 mt-6">
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Email *</label>
                  <input
                    type="email"
                    {...register("adminEmail", {
                      required: "Email is required",
                      pattern: {
                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                        message: "Invalid email format",
                      },
                    })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="john.doe@acme.com"
                  />
                  {errors.adminEmail && (
                    <p className="text-sm text-red-600 flex items-center mt-1">
                      <span className="w-4 h-4 mr-1">⚠️</span>
                      {errors.adminEmail.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">Username *</label>
                  <input
                    type="text"
                    {...register("adminUsername", {
                      required: "Username is required",
                      pattern: {
                        value: /^[a-zA-Z0-9_]+$/,
                        message:
                          "Username can only contain letters, numbers, and underscores",
                      },
                      minLength: {
                        value: 3,
                        message: "Username must be at least 3 characters",
                      },
                    })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="johndoe"
                  />
                  {errors.adminUsername && (
                    <p className="text-sm text-red-600 flex items-center mt-1">
                      <span className="w-4 h-4 mr-1">⚠️</span>
                      {errors.adminUsername.message}
                    </p>
                  )}
                </div>
              </div>

              <div className="mt-6 space-y-2">
                <label className="block text-sm font-medium text-gray-700">Password *</label>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    {...register("adminPassword", {
                      required: "Password is required",
                      minLength: {
                        value: 8,
                        message: "Password must be at least 8 characters",
                      },
                      pattern: {
                        value:
                          /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
                        message:
                          "Password must contain uppercase, lowercase, number, and special character",
                      },
                    })}
                    className="w-full px-4 py-3 pr-12 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors duration-200 bg-gray-50 focus:bg-white"
                    placeholder="SecurePass123!"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors duration-200"
                  >
                    {showPassword ? (
                      <EyeOff className="h-5 w-5" />
                    ) : (
                      <Eye className="h-5 w-5" />
                    )}
                  </button>
                </div>
                {errors.adminPassword && (
                  <p className="text-sm text-red-600 flex items-center mt-1">
                    <span className="w-4 h-4 mr-1">⚠️</span>
                    {errors.adminPassword.message}
                  </p>
                )}
              </div>
              </div>
            )}

            {/* Step 3: Subscription Plan */}
            {currentStep === 3 && (
              <div className="space-y-6">
                <div className="text-center mb-8">
                  <CreditCard className="h-12 w-12 text-primary-600 mx-auto mb-4" />
                  <h2 className="text-2xl font-bold text-gray-900 mb-2">Subscription Plan</h2>
                  <p className="text-gray-600">Choose the plan that fits your needs</p>
                </div>
              <div className="grid md:grid-cols-2 gap-6">
                <div className="relative bg-gradient-to-br from-primary-50 to-blue-50 border-2 border-primary-500 rounded-xl p-6 shadow-lg">
                  <div className="absolute -top-3 left-4 bg-primary-500 text-white px-3 py-1 rounded-full text-xs font-semibold">
                    RECOMMENDED
                  </div>
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-bold text-gray-900">Free Plan</h3>
                    <span className="text-2xl font-bold text-primary-600">$0<span className="text-sm font-normal text-gray-500">/month</span></span>
                  </div>
                  <ul className="text-sm text-gray-700 space-y-2 mb-6">
                    <li className="flex items-center"><span className="text-green-500 mr-2">✓</span> 5 Users</li>
                    <li className="flex items-center"><span className="text-green-500 mr-2">✓</span> Basic CRM</li>
                    <li className="flex items-center"><span className="text-green-500 mr-2">✓</span> Email Support</li>
                    <li className="flex items-center"><span className="text-green-500 mr-2">✓</span> 30-day Trial</li>
                  </ul>
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="radio"
                      {...register("subscriptionPlan")}
                      value="FREE"
                      defaultChecked
                      className="w-4 h-4 text-primary-600 border-gray-300 focus:ring-primary-500 mr-3"
                    />
                    <span className="text-sm font-medium text-gray-900">Select Free Plan</span>
                  </label>
                </div>

                <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-md hover:shadow-lg transition-shadow duration-200">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-bold text-gray-900">Starter Plan</h3>
                    <span className="text-2xl font-bold text-gray-900">$29<span className="text-sm font-normal text-gray-500">/month</span></span>
                  </div>
                  <ul className="text-sm text-gray-700 space-y-2 mb-6">
                    <li className="flex items-center"><span className="text-green-500 mr-2">✓</span> 25 Users</li>
                    <li className="flex items-center"><span className="text-green-500 mr-2">✓</span> Full CRM Suite</li>
                    <li className="flex items-center"><span className="text-green-500 mr-2">✓</span> Priority Support</li>
                    <li className="flex items-center"><span className="text-green-500 mr-2">✓</span> Advanced Reports</li>
                  </ul>
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="radio"
                      {...register("subscriptionPlan")}
                      value="STARTER"
                      className="w-4 h-4 text-primary-600 border-gray-300 focus:ring-primary-500 mr-3"
                    />
                    <span className="text-sm font-medium text-gray-900">Select Starter Plan</span>
                  </label>
                </div>
              </div>

                {/* Terms and Conditions */}
                <div className="bg-gray-50 rounded-xl p-6 space-y-4">
                  <div className="flex items-start">
                    <input
                      type="checkbox"
                      {...register("acceptTerms", {
                        required: "You must accept the terms and conditions",
                      })}
                      className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500 mt-1 mr-3"
                    />
                    <label className="text-sm text-gray-700 leading-relaxed">
                      I agree to the{" "}
                      <a
                        href="#"
                        className="text-primary-600 hover:text-primary-700 underline font-medium transition-colors duration-200"
                      >
                        Terms of Service
                      </a>{" "}
                      and{" "}
                      <a
                        href="#"
                        className="text-primary-600 hover:text-primary-700 underline font-medium transition-colors duration-200"
                      >
                        Privacy Policy
                      </a>
                    </label>
                  </div>
                  {errors.acceptTerms && (
                    <p className="text-sm text-red-600 flex items-center mt-2">
                      <span className="w-4 h-4 mr-1">⚠️</span>
                      {errors.acceptTerms.message}
                    </p>
                  )}

                  <div className="flex items-start">
                    <input
                      type="checkbox"
                      {...register("acceptMarketing")}
                      className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500 mt-1 mr-3"
                    />
                    <label className="text-sm text-gray-700 leading-relaxed">
                      I would like to receive marketing communications about product
                      updates and features (optional)
                    </label>
                  </div>
                </div>
              </div>
            )}

                </form>
              </div>
              
              {/* Navigation Controls */}
              <div className="flex justify-between items-center p-8 border-t border-gray-200 bg-gray-50">
                  <button
                    type="button"
                    onClick={prevStep}
                    disabled={currentStep === 1}
                    className={`flex items-center px-6 py-3 rounded-lg font-medium transition-all duration-200 ${
                      currentStep === 1
                        ? 'text-gray-400 cursor-not-allowed'
                        : 'text-gray-600 hover:text-gray-800 hover:bg-gray-100'
                    }`}
                  >
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    Previous
                  </button>

                  <div className="flex items-center space-x-2">
                    {steps.map((step) => (
                      <div
                        key={step.id}
                        className={`w-2 h-2 rounded-full transition-all duration-300 ${
                          currentStep >= step.id ? 'bg-primary-600' : 'bg-gray-300'
                        }`}
                      />
                    ))}
                  </div>

                {currentStep < totalSteps ? (
                  <button
                    type="button"
                    onClick={nextStep}
                    className="flex items-center px-6 py-3 bg-primary-600 hover:bg-primary-700 text-white rounded-lg font-medium transition-all duration-200 shadow-md hover:shadow-lg"
                  >
                    Next
                    <ArrowRight className="h-4 w-4 ml-2" />
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={handleSubmit(onSubmit)}
                    disabled={isSubmitting}
                    className="flex items-center px-8 py-3 bg-gradient-to-r from-primary-600 to-purple-600 hover:from-primary-700 hover:to-purple-700 text-white rounded-lg font-medium transition-all duration-200 shadow-lg hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {isSubmitting ? (
                      <>
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                        Creating...
                      </>
                    ) : (
                      <>
                        <Check className="h-4 w-4 mr-2" />
                        Create Organization
                      </>
                    )}
                  </button>
                )}
              </div>

              <p className="text-center text-sm text-gray-600 mt-4 px-8 pb-6">
                Already have an account?{" "}
                <Link
                  to="/login"
                  className="text-primary-600 hover:text-primary-700 font-semibold transition-colors duration-200"
                >
                  Sign in here
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TenantRegistration;
